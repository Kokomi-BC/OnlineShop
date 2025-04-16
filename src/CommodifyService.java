package src;

import java.util.Scanner;

public class CommodifyService {
    private static final Scanner scanner = new Scanner(System.in);

    public static void modifyCommodify(int id) {
        Commodify comm = CommodifyJDBC.getCommodifyById(id);
        if (comm == null) {
            System.out.println("未找到ID为 " + id + " 的商品");
            return;
        }
        System.out.println("当前商品信息:");
        System.out.println(comm);

        boolean continueEditing = true;
        while (continueEditing) {
            System.out.println("\n选择要修改的字段:");
            System.out.println("1. 名称 | 2. 类型 | 3. 详情 | 4. 价格 | 5. 库存 | 6. 产地 | 0. 完成");
            System.out.print("请选择: ");
            int choice = scanner.nextInt();
            scanner.nextLine(); // 消耗换行符
            switch (choice) {
                case 1:
                    System.out.print("新名称: ");
                    String newName = scanner.nextLine();
                    CommodifyJDBC.updateCommodifyPartial(id, "name", newName);
                    break;
                case 2:
                    System.out.print("新类型: ");
                    String newType = scanner.nextLine();
                    CommodifyJDBC.updateCommodifyPartial(id, "type", newType);
                    break;
                case 3:
                    System.out.print("新详情: ");
                    String newDetail = scanner.nextLine();
                    CommodifyJDBC.updateCommodifyPartial(id, "detail", newDetail);
                    break;
                case 4:
                    System.out.print("新价格: ");
                    double newPrice = scanner.nextDouble();
                    scanner.nextLine();
                    CommodifyJDBC.updateCommodifyPartial(id, "price", newPrice);
                    break;
                case 5:
                    System.out.print("新库存: ");
                    int newNum = scanner.nextInt();
                    scanner.nextLine();
                    CommodifyJDBC.updateCommodifyPartial(id, "num", newNum);
                    break;
                case 6:
                    System.out.print("新产地: ");
                    String newState = scanner.nextLine();
                    CommodifyJDBC.updateCommodifyPartial(id, "state", newState);
                    break;
                case 0:
                    continueEditing = false;
                    break;
                default:
                    System.out.println("无效选择");
            }

            // 显示更新后的信息
            System.out.println("\n更新后的商品信息:");
            System.out.println(CommodifyJDBC.getCommodifyById(id));
        }
    }
}