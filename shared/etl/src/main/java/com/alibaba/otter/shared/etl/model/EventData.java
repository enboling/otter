package com.alibaba.otter.shared.etl.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.builder.ToStringBuilder;

import com.alibaba.otter.shared.common.model.config.channel.ChannelParameter.SyncConsistency;
import com.alibaba.otter.shared.common.model.config.channel.ChannelParameter.SyncMode;
import com.alibaba.otter.shared.common.utils.OtterToStringStyle;

/**
 * 每条变更数据.
 * 
 * @author xiaoqing.zhouxq 2011-8-9 下午03:42:20
 */
public class EventData implements ObjectData, Serializable {

    private static final long serialVersionUID = -7071677425383765372L;

    /**
     * otter内部维护的一套tableId，区别与从eromanga中得到的tableId.
     */
    private long              tableId          = -1;

    private String            tableName;

    private String            schemaName;

    /**
     * 变更数据的业务类型(I/U/D/C/A/E),与ErosaProtocol中定义的EventType一致.
     */
    private EventType         eventType;

    /**
     * 变更数据的业务时间.
     */
    private long              executeTime;

    /**
     * 变更前的主键值,如果是insert/delete变更前和变更后的主键值是一样的.
     */
    private List<EventColumn> oldKeys          = new ArrayList<EventColumn>();

    /**
     * 变更后的主键值,如果是insert/delete变更前和变更后的主键值是一样的.
     */
    private List<EventColumn> keys             = new ArrayList<EventColumn>();

    private List<EventColumn> columns          = new ArrayList<EventColumn>();

    // ====================== 运行过程中对数据的附加属性 =============================
    /**
     * 预计的size大小，基于binlog event的推算
     */
    private long              size             = 1024;

    /**
     * 同步映射的id
     */
    private long              pairId           = -1;

    /**
     * 当eventType = CREATE/ALTER/ERASE时，就是对应的sql语句，否则无效.
     */
    private String            sql;

    /**
     * 自定义的同步模式, 允许覆盖默认的pipeline parameter，比如针对补救数据同步
     */
    private SyncMode          syncMode;

    /**
     * 自定义的同步一致性，允许覆盖默认的pipeline parameter，比如针对字段组强制反查数据库
     */
    private SyncConsistency   syncConsistency;

    /**
     * 是否为remedy补救数据，比如回环补救自动产生的数据，或者是freedom产生的手工订正数据
     */
    private boolean           remedy           = false;

    public long getTableId() {
        return tableId;
    }

    public void setTableId(long tableId) {
        this.tableId = tableId;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }

    public EventType getEventType() {
        return eventType;
    }

    public void setEventType(EventType eventType) {
        this.eventType = eventType;
    }

    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }

    public long getExecuteTime() {
        return executeTime;
    }

    public void setExecuteTime(long executeTime) {
        this.executeTime = executeTime;
    }

    public List<EventColumn> getKeys() {
        return keys;
    }

    public void setKeys(List<EventColumn> keys) {
        this.keys = keys;
    }

    public List<EventColumn> getColumns() {
        return columns;
    }

    public void setColumns(List<EventColumn> columns) {
        this.columns = columns;
    }

    public long getPairId() {
        return pairId;
    }

    public void setPairId(long pairId) {
        this.pairId = pairId;
    }

    public List<EventColumn> getOldKeys() {
        return oldKeys;
    }

    public void setOldKeys(List<EventColumn> oldKeys) {
        this.oldKeys = oldKeys;
    }

    public SyncMode getSyncMode() {
        return syncMode;
    }

    public void setSyncMode(SyncMode syncMode) {
        this.syncMode = syncMode;
    }

    public SyncConsistency getSyncConsistency() {
        return syncConsistency;
    }

    public void setSyncConsistency(SyncConsistency syncConsistency) {
        this.syncConsistency = syncConsistency;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public boolean isRemedy() {
        return remedy;
    }

    public void setRemedy(boolean remedy) {
        this.remedy = remedy;
    }

    // ======================== helper method =================

    /**
     * 返回所有待变更的字段
     */
    public List<EventColumn> getUpdatedColumns() {
        List<EventColumn> columns = new ArrayList<EventColumn>();
        for (EventColumn column : this.columns) {
            if (column.isUpdate()) {
                columns.add(column);
            }
        }

        return columns;
    }

    public String toString() {
        return ToStringBuilder.reflectionToString(this, OtterToStringStyle.DEFAULT_STYLE);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((columns == null) ? 0 : columns.hashCode());
        result = prime * result + ((eventType == null) ? 0 : eventType.hashCode());
        result = prime * result + (int) (executeTime ^ (executeTime >>> 32));
        result = prime * result + ((keys == null) ? 0 : keys.hashCode());
        result = prime * result + ((oldKeys == null) ? 0 : oldKeys.hashCode());
        result = prime * result + (int) (pairId ^ (pairId >>> 32));
        result = prime * result + ((schemaName == null) ? 0 : schemaName.hashCode());
        result = prime * result + (int) (tableId ^ (tableId >>> 32));
        result = prime * result + ((tableName == null) ? 0 : tableName.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        EventData other = (EventData) obj;
        if (columns == null) {
            if (other.columns != null) return false;
        } else if (!columns.equals(other.columns)) return false;
        if (eventType != other.eventType) return false;
        if (executeTime != other.executeTime) return false;
        if (keys == null) {
            if (other.keys != null) return false;
        } else if (!keys.equals(other.keys)) return false;
        if (oldKeys == null) {
            if (other.oldKeys != null) return false;
        } else if (!oldKeys.equals(other.oldKeys)) return false;
        if (pairId != other.pairId) return false;
        if (schemaName == null) {
            if (other.schemaName != null) return false;
        } else if (!schemaName.equals(other.schemaName)) return false;
        if (tableId != other.tableId) return false;
        if (tableName == null) {
            if (other.tableName != null) return false;
        } else if (!tableName.equals(other.tableName)) return false;
        return true;
    }

}
