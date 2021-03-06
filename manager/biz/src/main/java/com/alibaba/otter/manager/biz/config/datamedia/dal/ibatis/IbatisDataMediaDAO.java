package com.alibaba.otter.manager.biz.config.datamedia.dal.ibatis;

import java.util.List;
import java.util.Map;

import org.springframework.orm.ibatis.support.SqlMapClientDaoSupport;

import com.alibaba.otter.shared.common.utils.Assert;
import com.alibaba.otter.manager.biz.config.datamedia.dal.DataMediaDAO;
import com.alibaba.otter.manager.biz.config.datamedia.dal.dataobject.DataMediaDO;

/**
 * DataMedia的DAO层，ibatis的实现，主要是CRUD操作。
 * 
 * @author simon
 */
public class IbatisDataMediaDAO extends SqlMapClientDaoSupport implements DataMediaDAO {

    public DataMediaDO insert(DataMediaDO dataMedia) {
        Assert.assertNotNull(dataMedia);
        getSqlMapClientTemplate().insert("insertDataMedia", dataMedia);
        return dataMedia;
    }

    public void delete(Long dataMediaId) {
        Assert.assertNotNull(dataMediaId);
        getSqlMapClientTemplate().delete("deleteDataMediaById", dataMediaId);
    }

    public void update(DataMediaDO dataMedia) {
        Assert.assertNotNull(dataMedia);
        getSqlMapClientTemplate().update("updateDataMedia", dataMedia);
    }

    public boolean checkUnique(DataMediaDO dataMedia) {
        int count = (Integer) getSqlMapClientTemplate().queryForObject("checkDataMediaUnique", dataMedia);
        return count == 0 ? true : false;
    }

    public DataMediaDO checkUniqueAndReturnExist(DataMediaDO dataMedia) {
        return (DataMediaDO) getSqlMapClientTemplate().queryForObject("checkDataMediaUniqueAndReturnTheExist",
                                                                      dataMedia);
    }

    public DataMediaDO findById(Long dataMediaId) {
        Assert.assertNotNull(dataMediaId);
        return (DataMediaDO) getSqlMapClientTemplate().queryForObject("findDataMediaById", dataMediaId);
    }

    public List<DataMediaDO> listAll() {
        return (List<DataMediaDO>) getSqlMapClientTemplate().queryForList("listDataMedias");
    }

    public List<DataMediaDO> listByDataMediaSourceId(Long dataMediaSourceId) {
        Assert.assertNotNull(dataMediaSourceId);
        return (List<DataMediaDO>) getSqlMapClientTemplate().queryForList("listDataMediasByDataMediaSourceId",
                                                                          dataMediaSourceId);
    }

    public List<DataMediaDO> listByCondition(Map condition) {
        List<DataMediaDO> dataMediaDos = getSqlMapClientTemplate().queryForList("listDataMedias", condition);
        return dataMediaDos;
    }

    public List<DataMediaDO> listByMultiId(Long... identities) {
        List<DataMediaDO> dataMediaDos = getSqlMapClientTemplate().queryForList("listDataMediaByIds", identities);
        return dataMediaDos;
    }

    public int getCount() {
        Integer count = (Integer) getSqlMapClientTemplate().queryForObject("getDataMediaCount");
        return count.intValue();
    }

    public int getCount(Map condition) {
        Integer count = (Integer) getSqlMapClientTemplate().queryForObject("getDataMediaCount", condition);
        return count.intValue();
    }

}
