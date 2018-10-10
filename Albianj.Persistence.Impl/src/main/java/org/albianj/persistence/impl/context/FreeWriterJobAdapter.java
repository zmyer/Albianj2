/*
Copyright (c) 2016, Shanghai YUEWEN Information Technology Co., Ltd. 
All rights reserved.
Redistribution and use in source and binary forms, with or without modification, 
are permitted provided that the following conditions are met:

* Redistributions of source code must retain the above copyright notice, 
* this list of conditions and the following disclaimer.
* Redistributions in binary form must reproduce the above copyright notice, 
* this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
* Neither the name of Shanghai YUEWEN Information Technology Co., Ltd. 
* nor the names of its contributors may be used to endorse or promote products derived from 
* this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY SHANGHAI YUEWEN INFORMATION TECHNOLOGY CO., LTD. 
AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, 
BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. 
IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, 
OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; 
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, 
WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, 
EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

Copyright (c) 2016 著作权由上海阅文信息技术有限公司所有。著作权人保留一切权利。

这份授权条款，在使用者符合以下三条件的情形下，授予使用者使用及再散播本软件包装原始码及二进位可执行形式的权利，无论此包装是否经改作皆然：

* 对于本软件源代码的再散播，必须保留上述的版权宣告、此三条件表列，以及下述的免责声明。
* 对于本套件二进位可执行形式的再散播，必须连带以文件以及／或者其他附于散播包装中的媒介方式，重制上述之版权宣告、此三条件表列，以及下述的免责声明。
* 未获事前取得书面许可，不得使用柏克莱加州大学或本软件贡献者之名称，来为本软件之衍生物做任何表示支持、认可或推广、促销之行为。

免责声明：本软件是由上海阅文信息技术有限公司及本软件之贡献者以现状提供，本软件包装不负任何明示或默示之担保责任，
包括但不限于就适售性以及特定目的的适用性为默示性担保。加州大学董事会及本软件之贡献者，无论任何条件、无论成因或任何责任主义、
无论此责任为因合约关系、无过失责任主义或因非违约之侵权（包括过失或其他原因等）而起，对于任何因使用本软件包装所产生的任何直接性、间接性、
偶发性、特殊性、惩罚性或任何结果的损害（包括但不限于替代商品或劳务之购用、使用损失、资料损失、利益损失、业务中断等等），
不负任何责任，即在该种使用已获事前告知可能会造成此类损害的情形下亦然。
*/
package org.albianj.persistence.impl.context;

import org.albianj.persistence.context.IWriterJob;
import org.albianj.persistence.context.dactx.AlbianDataAccessOpt;
import org.albianj.persistence.context.dactx.IAlbianObjectWarp;
import org.albianj.persistence.db.AlbianDataServiceException;
import org.albianj.persistence.impl.db.CreateCommandAdapter;
import org.albianj.persistence.impl.db.IPersistenceUpdateCommand;
import org.albianj.persistence.impl.db.ModifyCommandAdapter;
import org.albianj.persistence.impl.db.RemoveCommandAdapter;
import org.albianj.persistence.object.*;

import java.beans.PropertyDescriptor;
import java.util.List;
import java.util.Map;

public abstract class FreeWriterJobAdapter implements IWriterJobAdapter {
    public IWriterJob buildCreation(String sessionId, IAlbianObject object) throws AlbianDataServiceException {
        IWriterJob job = new WriterJob(sessionId);
        IPersistenceUpdateCommand cca = new CreateCommandAdapter();
        buildWriterJob(job,object,null,null, cca);
        return job;
    }

    public IWriterJob buildCreation(String sessionId, List<? extends IAlbianObject> objects) throws AlbianDataServiceException {
        IWriterJob job = new WriterJob(sessionId);
        IPersistenceUpdateCommand cca = new CreateCommandAdapter();
        for (IAlbianObject object : objects) {
            buildWriterJob(job,object,null,null, cca);
        }
        return job;
    }

    public IWriterJob buildModification(String sessionId, IAlbianObject object) throws AlbianDataServiceException {
        IWriterJob job = new WriterJob(sessionId);
        IPersistenceUpdateCommand mca = new ModifyCommandAdapter();
        buildWriterJob(job,object,null,null, mca);
        return job;
    }

//    public IWriterJob buildModification(String sessionId, IAlbianObject object, String[] members) throws AlbianDataServiceException {
//        IWriterJob job = new WriterJob(sessionId);
//        IPersistenceUpdateCommand mca = new ModifyCommandAdapter();
//        buildWriterJob(object, job, mca, members);
//        return job;
//    }


    public IWriterJob buildModification(String sessionId, List<? extends IAlbianObject> objects) throws AlbianDataServiceException {
        IWriterJob job = new WriterJob(sessionId);
        IPersistenceUpdateCommand mca = new ModifyCommandAdapter();
        for (IAlbianObject object : objects) {
            buildWriterJob(job,object,null,null, mca);

        }
        return job;
    }

    public IWriterJob buildRemoved(String sessionId, IAlbianObject object) throws AlbianDataServiceException {
        IWriterJob job = new WriterJob(sessionId);
        IPersistenceUpdateCommand rca = new RemoveCommandAdapter();
        buildWriterJob(job,object,null,null, rca);

        return job;
    }

    public IWriterJob buildRemoved(String sessionId, List<? extends IAlbianObject> objects) throws AlbianDataServiceException {
        IWriterJob job = new WriterJob(sessionId);
        IPersistenceUpdateCommand rca = new RemoveCommandAdapter();
        for (IAlbianObject object : objects) {
            buildWriterJob(job,object,null,null, rca);
        }
        return job;
    }

    public IWriterJob buildSaving(String sessionId, IAlbianObject object) throws AlbianDataServiceException {
        IWriterJob job = new WriterJob(sessionId);
        IPersistenceUpdateCommand iuc;
        if (object.getIsAlbianNew()) {
            iuc = new CreateCommandAdapter();
        } else {
            iuc = new ModifyCommandAdapter();
        }

        buildWriterJob(job,object,null,null, iuc);
        return job;
    }

    public IWriterJob buildSaving(String sessionId, List<? extends IAlbianObject> objects) throws AlbianDataServiceException {
        IWriterJob job = new WriterJob(sessionId);
        IPersistenceUpdateCommand cca = new CreateCommandAdapter();
        IPersistenceUpdateCommand mca = new ModifyCommandAdapter();
        for (IAlbianObject object : objects) {
            if (object.getIsAlbianNew()) {
                buildWriterJob(job,object,null,null, cca);
            } else {
                buildWriterJob(job,object,null,null, mca);
            }
        }
        return job;
    }

//    protected abstract void buildWriterJob(IAlbianObject object,
//                                           IWriterJob writerJob, IPersistenceUpdateCommand update) throws AlbianDataServiceException;
//
//    protected abstract void buildWriterJob(IAlbianObject object,
//                                           IWriterJob writerJob, IPersistenceUpdateCommand update, String[] members) throws AlbianDataServiceException;

//    protected abstract Map<String, Object> buildSqlParameter(String jobId,
//                                                             IAlbianObject object, IAlbianObjectAttribute albianObject,
//                                                             PropertyDescriptor[] propertyDesc) throws AlbianDataServiceException;

//    protected abstract List<IDataRouterAttribute> parserRoutings(String jobId,
//                                                                 IAlbianObject object, IDataRoutersAttribute routings,
//                                                                 IAlbianObjectAttribute albianObject);
//
//    protected abstract String parserRoutingStorage(String jobId, IAlbianObject obj,
//                                                   IDataRouterAttribute routing, IAlbianObjectDataRouter hashMapping,
//                                                   IAlbianObjectAttribute albianObject) throws AlbianDataServiceException;



    public IWriterJob buildWriterJob(String sessionId, List<IAlbianObjectWarp> entities)
            throws AlbianDataServiceException{
        IWriterJob job = new WriterJob(sessionId);
        IPersistenceUpdateCommand crtCmd = new CreateCommandAdapter();
        IPersistenceUpdateCommand mdfCmd = new ModifyCommandAdapter();
        IPersistenceUpdateCommand dltCmd = new RemoveCommandAdapter();
        for(IAlbianObjectWarp entity : entities){
            switch (entity.getPersistenceOpt()){
                case (AlbianDataAccessOpt.Create): {
                    buildWriterJob(job,entity.getEntry(),entity.getStorageAliasName(),entity.getTableAliasName(), crtCmd);
                    break;
                }
                case AlbianDataAccessOpt.Update:{
                    buildWriterJob(job,entity.getEntry(),entity.getStorageAliasName(),entity.getTableAliasName(), mdfCmd);
                    break;
                }
                case AlbianDataAccessOpt.Delete :{
                    buildWriterJob(job,entity.getEntry(),entity.getStorageAliasName(),entity.getTableAliasName(), dltCmd);
                    break;
                }
                case AlbianDataAccessOpt.Save:
                    default:{
                        if(entity.getEntry().getIsAlbianNew()){
                            buildWriterJob(job,entity.getEntry(),entity.getStorageAliasName(),entity.getTableAliasName(), crtCmd);
                        } else {
                            buildWriterJob(job,entity.getEntry(),entity.getStorageAliasName(),entity.getTableAliasName(), mdfCmd);
                        }
                        break;
                }
            }
        }

        return job;
    }

    protected abstract void buildWriterJob(IWriterJob job,IAlbianObject entity,
                                           String storageAlias,String tableAlias,
                                           IPersistenceUpdateCommand cmd);




}
