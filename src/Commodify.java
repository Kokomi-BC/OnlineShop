package src;
public class Commodify {
    private int id;
    private String name;
    private String type;
    private String detail;
    private double price;
    private int num;
    private String state;

    public Commodify() {
    }

    public Commodify(int id, String name, String type, String detail, double price, int num, String state) {
        super();
        this.id = id;
        this.name = name;
        this.type = type;
        this.detail = detail;
        this.price = price;
        this.num = num;
        this.state = state;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getNum() {
        return num;
    }

    public void setNum(int num) {
        this.num = num;
    }

    public String getState() {
        return state;
    }

    public void setstate(String state) {
        this.state = state;
    }

    @Override
    public String toString() {
        return  "商品编号[" + id + "]    商品名称[" + name + "]    商品类型[" + type + "]    商品详情[" + detail + "]    商品价格[" + price + "]    商品库存[" + num + "]    商品产地[" + state + "]";
    }


}

