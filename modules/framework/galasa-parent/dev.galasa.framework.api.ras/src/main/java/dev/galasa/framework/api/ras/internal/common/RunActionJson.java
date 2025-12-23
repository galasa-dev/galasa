package dev.galasa.framework.api.ras.internal.common;

public class RunActionJson {

    private String status;
    private String result;
    private String[] tags;

    public RunActionJson(String status, String result, String[] tags){
        this.status = status;
        this.result = result;
        this.tags = tags;
    }
    
    public String getStatus(){
        return this.status;
    }

    public String getResult(){
        return this.result;
    }

    public String[] getTags(){
        return this.tags;
    }
}
