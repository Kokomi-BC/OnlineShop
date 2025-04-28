package src;
import java.sql.Date;
import java.util.List;

public class example {

    public static void printCommodityList(List<Commodity> commodities) {
        if (commodities == null || commodities.isEmpty()) {
            System.out.println("商品列表为空");
            return;
        }
        System.out.println("======= 查询结果 =======");
        System.out.println("共找到 " + commodities.size() + " 个商品");
        System.out.println("=======================");
        int commodityCount = 1;
        for (Commodity c : commodities) {
            System.out.println("\n商品 #" + commodityCount++);
            System.out.println("-----------------------");
            System.out.println("ID: " + c.getId());
            System.out.println("名称: " + c.getName());
            System.out.println("类型: " + c.getType());
            System.out.println("详情: " + c.getDetail());
            System.out.println("生产日期: " + c.getProductionDate());
            System.out.println("制造商: " + c.getManufacturer());
            System.out.println("产地: " + c.getOrigin());
            System.out.println("备注: " + c.getRemark());
            List<CommoditySKU> skus = c.getSkus();
            if (skus == null || skus.isEmpty()) {
                System.out.println("该商品暂无SKU信息");
            } else {
                System.out.println("包含 " + skus.size() + " 个SKU:");
                int skuCount = 1;
                for (CommoditySKU sku : skus) {
                    System.out.println("  SKU #" + skuCount++);
                    System.out.println("  -----------");
                    System.out.println("  SKU ID: " + sku.getSkuId());
                    System.out.println("  颜色: " + sku.getColor());
                    System.out.println("  款式: " + sku.getStyle());
                    System.out.printf("  价格: ¥%.2f%n", sku.getPrice());
                    System.out.println("  库存: " + sku.getStock());
                    System.out.println();
                }
            }
        }
    }
    public static void main(String[] args) {
        // 添加示例商品（修正参数顺序问题）
      /*  CommodityJDBC.addCommodityWithSKUs(
                new Commodity(
                        4,
                        "华为Matepad11",
                        "数码产品",
                        "旗舰平板",
                        Date.valueOf("2023-09-16"),
                        "华为技术有限公司",
                        "中国深圳",
                        "卫星通信"
                ),
                List.of(
                        // 构造函数参数顺序为：color, style, stock, price
                        new CommoditySKU("银色色", "16+512G", 500, 3999.00)
                )
        );*/


        CommodityJDBC.addCommodity(
                new Commodity(
                        "华为Matepad11",
                        "数码产品",
                        "旗舰平板",
                        Date.valueOf("2023-09-16"),
                        "华为技术有限公司",
                        "中国深圳",
                        "卫星通信"
                ),
                List.of(
                        // 构造函数参数顺序为：color, style, stock, price
                        new CommoditySKU("银色", "16+512G", 500, 3999.00)
                )
        );
        // 测试查询功能
        Commodity commodity = CommodityJDBC.getCommodityById(2);
        List<Commodity> commodities = CommodityJDBC.getCommodityByName("华为Matepad");
        printCommodityList(commodities);

        // 安全打印商品详情
        if (commodity != null) {
            System.out.println("\n单个商品详情:");
            System.out.println(commodity.toString());
        } else {
            System.out.println("未找到ID为1的商品");
        }
    }
}