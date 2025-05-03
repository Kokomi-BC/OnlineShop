package src;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Date;

public class User {
    private int id;
    private String username;
    private String password;
    private String phone;
    private String address;
    private BigDecimal balance;
    private String remark;
    private Date createdTime;
    private Date modifiedTime;

    public User(String username, String password) {  this.username = username;
        this.password = password;}


    public User(String username, String password, String phone, String address, BigDecimal balance, String remark) {
        this.username = username;
        this.password = password;
        this.phone = phone;
        this.address = address;
        this.balance = balance;
        this.remark = remark;
    }
    public User(int id, String username, String password, String phone, String address, BigDecimal balance){
        this.id=id;
        this.username = username;
        this.password = password;
        this.phone = phone;
        this.address = address;
        this.balance = balance;
    }
    public User(int id, String username, String password, String phone, String address, BigDecimal balance, String remark, Timestamp createdTime, Timestamp modifiedTime) {
        this.id=id;
        this.username = username;
        this.password = password;
        this.phone = phone;
        this.address = address;
        this.balance = balance;
        this.remark = remark;
        this.createdTime=createdTime;
        this.modifiedTime=modifiedTime;
    }

    public User(int id, String username, String password, String phone, String address, BigDecimal balance, String remark) {
        this.id=id;
        this.username = username;
        this.password = password;
        this.phone = phone;
        this.address = address;
        this.balance = balance;
        this.remark = remark;
    }

    public User() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public Date getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Date createdTime) {
        this.createdTime = createdTime;
    }

    public Date getModifiedTime() {
        return modifiedTime;
    }

    public void setModifiedTime(Date modifiedTime) {
        this.modifiedTime = modifiedTime;
    }

    // toString() 方法
    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", phone='" + phone + '\'' +
                ", address='" + address + '\'' +
                ", balance=" + balance +
                ", remark='" + remark + '\'' +
                ", createdTime=" + createdTime +
                ", modifiedTime=" + modifiedTime +
                '}';
    }
}
