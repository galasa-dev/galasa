package dev.galasa.framework.api.ras.internal.common;

public class RunActionJson {

    private String status;
    private String result;

    public RunActionJson(String status, String result){
        this.status = status;
        this.result = result;
    }
    
    public String getStatus(){
        return this.status;
    }

    public String getResult(){
        return this.result;
    }
}
