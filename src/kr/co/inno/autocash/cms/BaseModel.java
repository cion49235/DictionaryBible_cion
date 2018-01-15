package kr.co.inno.autocash.cms;

public class BaseModel {
    public String code;
    public String msg;

    @Override
    public String toString() {
        return "BaseModel{" +
                "code='" + code + '\'' +
                ", msg='" + msg + '\'' +
                '}';
    }
}
