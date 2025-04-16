package src;

public class example {
        public static void main(String[] args) {
            // 删除商品
            CommodifyJDBC.deleteCommodify(1);
            // 添加示例商品
            CommodifyJDBC.addCommodify(new Commodify(1, "iPhone 13", "手机", "苹果手机", 5999, 100, "美国"));

            // 修改商品
            CommodifyService.modifyCommodify(1);


        }
    }
