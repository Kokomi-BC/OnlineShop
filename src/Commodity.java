package src;
import java.math.BigDecimal;
import java.util.*;

public class Commodity {
    private int id;
    private String name;
    private String type;
    private String detail;
    private String imageBase64;
    private Date productionDate;
    private String manufacturer;
    private String origin;
    private String remark;
    private List<CommoditySKU> skus;

    public Commodity(int id, String name, String type, String detail, Date productionDate,
                     String manufacturer, String origin, String remark, String imageBase64, List<CommoditySKU> skus) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.detail = detail;
        this.productionDate = productionDate;
        this.manufacturer = manufacturer;
        this.origin = origin;
        this.remark = remark;
        this.imageBase64 = imageBase64;
        this.skus = skus;
    }
    public Commodity(int id, String name, String type, String detail, Date productionDate,
                     String manufacturer, String origin, String remark, List<CommoditySKU> skus) {
        this(id, name, type, detail, productionDate, manufacturer, origin, remark, null, skus);
    }
    public Commodity(int id, String name, String type, String detail, Date productionDate,
                     String manufacturer, String origin, String remark, String imageBase64) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.detail = detail;
        this.productionDate = productionDate;
        this.manufacturer = manufacturer;
        this.origin = origin;
        this.remark = remark;
        this.imageBase64 = imageBase64;
    }
    public Commodity(int id, String name, String type, String detail, Date productionDate,
                     String manufacturer, String origin, String remark) {
        this(id, name, type, detail, productionDate, manufacturer, origin, remark, (List<CommoditySKU>) null);
    }
    public Commodity(String name, String type, String detail, Date productionDate,
                     String manufacturer, String origin, String remark, String imageBase64) {
        this.name = name;
        this.type = type;
        this.detail = detail;
        this.productionDate = productionDate;
        this.manufacturer = manufacturer;
        this.origin = origin;
        this.remark = remark;
        this.imageBase64 = imageBase64;
    }
    public Commodity(String name, String type, String detail, Date productionDate,
                     String manufacturer, String origin, String remark) {
        this(name, type, detail, productionDate, manufacturer, origin, remark, null);
    }
    // 无参构造方法
    public Commodity() {
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

    public String getImageBase64() {
        return imageBase64;
    }

    public void setImageBase64(String imageBase64) {
        this.imageBase64 = imageBase64;
    }

    public Date getProductionDate() {
        return productionDate;
    }

    public void setProductionDate(Date productionDate) {
        this.productionDate = productionDate;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public List<CommoditySKU> getSkus() {
        return skus;
    }

    public void setSkus(List<CommoditySKU> skus) {
        this.skus = skus;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Commodity commodity = (Commodity) o;
        return id == commodity.id &&
                Objects.equals(name, commodity.name) &&
                Objects.equals(type, commodity.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, type);
    }

    // toString 方法
    @Override
    public String toString() {
        return "Commodity{" +
                "id=" + id +
                ", 名称='" + name + '\'' +
                ", 类型='" + type + '\'' +
                ", 图片长度=" + (imageBase64 == null ? 0 : imageBase64.length()) +
                ",产品日期=" + productionDate +
                ", 品牌='" + manufacturer + '\'' +
                ", 总库存=" + (skus == null || skus.isEmpty() ? "null" :  getTotalStock ()    ) +
                '}';
    }
    public void addSku(CommoditySKU sku) {
        if (sku == null) return;
        if (sku.getStock() < 0 || sku.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("库存不能为负数且价格必须大于零");
        }
            if (skus == null) {
                skus = new ArrayList<>();
            }
            try {
                sku.setCommodityid(this.id); // 设置关联ID
                skus.add(sku);
            } catch (IllegalArgumentException e) {
                // 处理错误情况
                throw new RuntimeException("无法添加商品详情: " + e.getMessage());
            }
        }

        // 获取所有SKU的总库存
        public int getTotalStock () {
            if (skus == null || skus.isEmpty()) {
                return 0;
            }
            return skus.stream().mapToInt(CommoditySKU::getStock).sum();
        }



}