package com.alibaba.otter.node.etl.common.pipe.impl.http;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ClassUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.util.CollectionUtils;

import com.alibaba.otter.node.etl.common.io.EncryptUtils;
import com.alibaba.otter.node.etl.common.io.EncryptedData;
import com.alibaba.otter.node.etl.common.io.download.DataRetriever;
import com.alibaba.otter.node.etl.common.pipe.PipeDataType;
import com.alibaba.otter.node.etl.common.pipe.exception.PipeException;
import com.alibaba.otter.node.etl.model.protobuf.BatchProto;
import com.alibaba.otter.shared.common.model.config.channel.ChannelParameter.SyncConsistency;
import com.alibaba.otter.shared.common.model.config.channel.ChannelParameter.SyncMode;
import com.alibaba.otter.shared.common.model.config.pipeline.Pipeline;
import com.alibaba.otter.shared.common.utils.ByteUtils;
import com.alibaba.otter.shared.common.utils.JsonUtils;
import com.alibaba.otter.shared.common.utils.NioUtils;
import com.alibaba.otter.shared.etl.model.DbBatch;
import com.alibaba.otter.shared.etl.model.EventColumn;
import com.alibaba.otter.shared.etl.model.EventData;
import com.alibaba.otter.shared.etl.model.EventType;
import com.alibaba.otter.shared.etl.model.FileBatch;
import com.alibaba.otter.shared.etl.model.FileData;
import com.alibaba.otter.shared.etl.model.Identity;
import com.alibaba.otter.shared.etl.model.RowBatch;

/**
 * 基于http下载的pipe实现
 * 
 * @author jianghang 2011-10-13 下午06:31:13
 * @version 4.0.0
 */
public class RowDataHttpPipe extends AbstractHttpPipe<DbBatch, HttpPipeKey> {

    public HttpPipeKey put(final DbBatch data) throws PipeException {
        return saveDbBatch(data);
    }

    public DbBatch get(final HttpPipeKey key) throws PipeException {
        // 处理dbBatch数据
        return getDbBatch(key);
    }

    // ======================== help method ===================
    // 保存对应的dbBatch
    private HttpPipeKey saveDbBatch(DbBatch dbBatch) {
        Map json = new HashMap();// 序列化对象
        RowBatch rowBatch = dbBatch.getRowBatch();
        // 转化为proto对象
        BatchProto.RowBatch.Builder rowBatchBuilder = BatchProto.RowBatch.newBuilder();
        rowBatchBuilder.setIdentity(build(rowBatch.getIdentity()));
        // 处理具体的字段rowData
        for (EventData eventData : rowBatch.getDatas()) {
            BatchProto.RowData.Builder rowDataBuilder = BatchProto.RowData.newBuilder();
            rowDataBuilder.setPairId(eventData.getPairId());
            rowDataBuilder.setTableId(eventData.getTableId());
            if (eventData.getSchemaName() != null) {
                rowDataBuilder.setSchemaName(eventData.getSchemaName());
            }
            rowDataBuilder.setTableName(eventData.getTableName());
            rowDataBuilder.setEventType(eventData.getEventType().getValue());
            rowDataBuilder.setExecuteTime(eventData.getExecuteTime());
            // add by ljh at 2012-10-31
            if (eventData.getSyncMode() != null) {
                rowDataBuilder.setSyncMode(eventData.getSyncMode().getValue());
            }
            if (eventData.getSyncConsistency() != null) {
                rowDataBuilder.setSyncConsistency(eventData.getSyncConsistency().getValue());
            }

            // 构造key column
            for (EventColumn keyColumn : eventData.getKeys()) {
                rowDataBuilder.addKeys(buildColumn(keyColumn));
            }
            // 构造old key column
            if (CollectionUtils.isEmpty(eventData.getOldKeys()) == false) {
                for (EventColumn keyColumn : eventData.getOldKeys()) {
                    rowDataBuilder.addOldKeys(buildColumn(keyColumn));
                }
            }

            // 构造其他 column
            for (EventColumn column : eventData.getColumns()) {
                rowDataBuilder.addColumns(buildColumn(column));
            }

            rowDataBuilder.setRemedy(eventData.isRemedy());
            rowDataBuilder.setSize(eventData.getSize());
            rowBatchBuilder.addRows(rowDataBuilder.build());// 添加一条rowData记录
        }

        json.put(ClassUtils.getShortClassName(rowBatch.getClass()), rowBatchBuilder.build().toByteArray());

        // 处理下FileBatch
        FileBatch fileBatch = dbBatch.getFileBatch();
        if (fileBatch != null) {
            BatchProto.FileBatch.Builder fileBatchBuilder = BatchProto.FileBatch.newBuilder();
            fileBatchBuilder.setIdentity(build(fileBatch.getIdentity()));
            // 构造对应的proto对象
            for (FileData fileData : fileBatch.getFiles()) {
                BatchProto.FileData.Builder fileDataBuilder = BatchProto.FileData.newBuilder();
                fileDataBuilder.setPairId(fileData.getPairId());
                fileDataBuilder.setTableId(fileData.getTableId());
                if (fileData.getNameSpace() != null) {
                    fileDataBuilder.setNamespace(fileData.getNameSpace());
                }
                if (fileData.getPath() != null) {
                    fileDataBuilder.setPath(fileData.getPath());
                }
                fileDataBuilder.setEventType(fileData.getEventType().getValue());
                fileDataBuilder.setSize(fileData.getSize());
                fileDataBuilder.setLastModifiedTime(fileData.getLastModifiedTime());

                fileBatchBuilder.addFiles(fileDataBuilder.build());// 添加一条fileData记录
            }

            // 放到json中
            json.put(ClassUtils.getShortClassName(fileBatch.getClass()), fileBatchBuilder.build().toByteArray());
        }

        // 进行持久化
        byte[] bytes = JsonUtils.marshalToByte(json);// 序列化
        EncryptedData encryptedData = EncryptUtils.encrypt(bytes);
        // 处理构造对应的文件url
        String filename = buildFileName(rowBatch.getIdentity(), ClassUtils.getShortClassName(dbBatch.getClass()));
        File file = new File(htdocsDir, filename);
        try {
            NioUtils.write(encryptedData.getData(), file);
        } catch (IOException e) {
            throw new PipeException("write_byte_error", e);
        }

        HttpPipeKey key = new HttpPipeKey();
        key.setCrc(encryptedData.getCrc());
        key.setKey(encryptedData.getKey());
        key.setUrl(remoteUrlBuilder.getUrl(rowBatch.getIdentity().getPipelineId(), filename));
        key.setDataType(PipeDataType.DB_BATCH);
        key.setIdentity(rowBatch.getIdentity());
        return key;
    }

    // 处理对应的dbBatch
    private DbBatch getDbBatch(HttpPipeKey key) {
        String dataUrl = key.getUrl();
        Pipeline pipeline = configClientService.findPipeline(key.getIdentity().getPipelineId());
        DataRetriever dataRetriever = dataRetrieverFactory.createRetriever(pipeline.getParameters().getRetriever(),
                                                                           dataUrl, downloadDir);
        File archiveFile = null;
        try {
            dataRetriever.connect();
            dataRetriever.doRetrieve();
            archiveFile = dataRetriever.getDataAsFile();
        } catch (Exception e) {
            dataRetriever.abort();
            throw new PipeException("download_error", e);
        } finally {
            dataRetriever.disconnect();
        }

        try {
            byte[] bytes = NioUtils.read(archiveFile);
            EncryptedData encryptedData = new EncryptedData(bytes, key.getKey(), key.getCrc());
            byte[] protobytes = EncryptUtils.decrypt(encryptedData);
            Map data = JsonUtils.unmarshalFromByte(protobytes, Map.class);

            String rowBatchKey = ClassUtils.getShortClassName(RowBatch.class);
            // fastjson bug，byte[]数据无法正常反序列，base64手工反解密
            byte[] rowBatchBytes = ByteUtils.base64StringToBytes((String) data.get(rowBatchKey));
            DbBatch dbBatch = new DbBatch();
            if (rowBatchBytes != null) {
                BatchProto.RowBatch rowbatchProto = BatchProto.RowBatch.parseFrom(rowBatchBytes);
                // 构造原始的model对象
                RowBatch rowBatch = new RowBatch();
                rowBatch.setIdentity(build(rowbatchProto.getIdentity()));
                for (BatchProto.RowData rowDataProto : rowbatchProto.getRowsList()) {
                    EventData eventData = new EventData();
                    eventData.setPairId(rowDataProto.getPairId());
                    eventData.setTableId(rowDataProto.getTableId());
                    eventData.setTableName(rowDataProto.getTableName());
                    eventData.setSchemaName(rowDataProto.getSchemaName());
                    eventData.setEventType(EventType.valuesOf(rowDataProto.getEventType()));
                    eventData.setExecuteTime(rowDataProto.getExecuteTime());
                    // add by ljh at 2012-10-31
                    if (StringUtils.isNotEmpty(rowDataProto.getSyncMode())) {
                        eventData.setSyncMode(SyncMode.valuesOf(rowDataProto.getSyncMode()));
                    }
                    if (StringUtils.isNotEmpty(rowDataProto.getSyncConsistency())) {
                        eventData.setSyncConsistency(SyncConsistency.valuesOf(rowDataProto.getSyncConsistency()));
                    }
                    // 处理主键
                    List<EventColumn> keys = new ArrayList<EventColumn>();
                    for (BatchProto.Column columnProto : rowDataProto.getKeysList()) {
                        keys.add(buildColumn(columnProto));
                    }
                    eventData.setKeys(keys);
                    // 处理old主键
                    if (CollectionUtils.isEmpty(rowDataProto.getOldKeysList()) == false) {
                        List<EventColumn> oldKeys = new ArrayList<EventColumn>();
                        for (BatchProto.Column columnProto : rowDataProto.getKeysList()) {
                            oldKeys.add(buildColumn(columnProto));
                        }
                        eventData.setOldKeys(oldKeys);
                    }
                    // 处理具体的column value
                    List<EventColumn> columns = new ArrayList<EventColumn>();
                    for (BatchProto.Column columnProto : rowDataProto.getColumnsList()) {
                        columns.add(buildColumn(columnProto));
                    }
                    eventData.setColumns(columns);

                    eventData.setRemedy(rowDataProto.getRemedy());
                    eventData.setSize(rowDataProto.getSize());
                    // 添加到总记录
                    rowBatch.merge(eventData);
                }
                dbBatch.setRowBatch(rowBatch);
            }
            String fileBatchKey = ClassUtils.getShortClassName(FileBatch.class);
            String fileBatchStr = (String) data.get(fileBatchKey);
            if (StringUtils.isNotEmpty(fileBatchStr)) { // 判断下是否存在file batch记录
                byte[] fileBatchBytes = ByteUtils.base64StringToBytes(fileBatchStr);
                if (fileBatchBytes != null) {
                    BatchProto.FileBatch filebatchProto = BatchProto.FileBatch.parseFrom(fileBatchBytes);
                    // 构造原始的model对象
                    FileBatch fileBatch = new FileBatch();
                    fileBatch.setIdentity(build(filebatchProto.getIdentity()));
                    for (BatchProto.FileData fileDataProto : filebatchProto.getFilesList()) {
                        FileData fileData = new FileData();
                        fileData.setPairId(fileDataProto.getPairId());
                        fileData.setTableId(fileDataProto.getTableId());
                        fileData.setEventType(EventType.valuesOf(fileDataProto.getEventType()));
                        fileData.setLastModifiedTime(fileDataProto.getLastModifiedTime());
                        fileData.setNameSpace(fileDataProto.getNamespace());
                        fileData.setPath(fileDataProto.getPath());
                        fileData.setSize(fileDataProto.getSize());
                        // 添加到filebatch中
                        fileBatch.getFiles().add(fileData);
                    }
                    dbBatch.setFileBatch(fileBatch);
                }
            }

            return dbBatch;
        } catch (IOException e) {
            throw new PipeException("deserial_error", e);
        }
    }

    private EventColumn buildColumn(BatchProto.Column columnProto) {
        EventColumn column = new EventColumn();
        column.setColumnName(columnProto.getName());
        column.setNull(columnProto.getIsNull());
        column.setColumnType(columnProto.getType());
        column.setColumnValue(columnProto.getValue());
        column.setKey(columnProto.getIsPrimaryKey());
        column.setIndex(columnProto.getIndex());
        column.setUpdate(columnProto.getIsUpdate());// add by ljh 2012-08-30，标记变更字段
        return column;
    }

    private BatchProto.Column buildColumn(EventColumn keyColumn) {
        BatchProto.Column.Builder columnBuilder = BatchProto.Column.newBuilder();
        columnBuilder.setName(keyColumn.getColumnName());
        columnBuilder.setType(keyColumn.getColumnType());
        columnBuilder.setIsNull(keyColumn.isNull());
        columnBuilder.setIsPrimaryKey(keyColumn.isKey());
        columnBuilder.setIndex(keyColumn.getIndex());
        if (keyColumn.getColumnValue() != null) {
            columnBuilder.setValue(keyColumn.getColumnValue());
        }
        columnBuilder.setIsUpdate(keyColumn.isUpdate());// add by ljh 2012-08-30，标记变更字段
        return columnBuilder.build();
    }

    // 构造文件名
    private String buildFileName(Identity identity, String prefix) {
        Date now = new Date();
        String time = new SimpleDateFormat(DATE_FORMAT).format(now);
        return MessageFormat.format("{0}-{1}-{2}-{3}-{4}.gzip", prefix, time, String.valueOf(identity.getChannelId()),
                                    String.valueOf(identity.getPipelineId()), String.valueOf(identity.getProcessId()));
    }

    // 构造proto对象
    private BatchProto.Identity build(Identity identity) {
        BatchProto.Identity.Builder identityBuilder = BatchProto.Identity.newBuilder();
        identityBuilder.setChannelId(identity.getChannelId());
        identityBuilder.setPipelineId(identity.getPipelineId());
        identityBuilder.setProcessId(identity.getProcessId());
        return identityBuilder.build();
    }

    // 从proto对象构造回object
    private Identity build(BatchProto.Identity identityProto) {
        Identity identity = new Identity();
        identity.setChannelId(identityProto.getChannelId());
        identity.setPipelineId(identityProto.getPipelineId());
        identity.setProcessId(identityProto.getProcessId());
        return identity;
    }

}
