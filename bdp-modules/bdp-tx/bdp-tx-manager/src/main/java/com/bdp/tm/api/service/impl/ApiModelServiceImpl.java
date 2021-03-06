package com.bdp.tm.api.service.impl;

import org.springframework.stereotype.Service;

import com.bdp.tm.api.service.ApiModelService;
import com.bdp.tm.manager.ModelInfoManager;
import com.bdp.tm.model.ModelInfo;

import java.util.List;

@Service
public class ApiModelServiceImpl implements ApiModelService {


    @Override
    public List<ModelInfo> onlines() {
        return ModelInfoManager.getInstance().getOnlines();
    }


}
