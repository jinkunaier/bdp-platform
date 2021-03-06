package com.bdp.tm.compensate.service;

import com.bdp.tm.compensate.model.TransactionCompensateMsg;
import com.bdp.tm.compensate.model.TxModel;
import com.bdp.tm.model.ModelName;
import com.bdp.tm.netty.model.TxGroup;
import com.bdp.tx.commons.exception.ServiceException;

import java.util.List;

public interface CompensateService {

    boolean saveCompensateMsg(TransactionCompensateMsg transactionCompensateMsg);

    List<ModelName> loadModelList();

    List<String> loadCompensateTimes(String model);

    List<TxModel> loadCompensateByModelAndTime(String path);

    void autoCompensate(String compensateKey, TransactionCompensateMsg transactionCompensateMsg);

    boolean executeCompensate(String key) throws ServiceException;

    void reloadCompensate(TxGroup txGroup);

    boolean hasCompensate();

    boolean delCompensate(String path);

    TxGroup  getCompensateByGroupId(String groupId);
}
