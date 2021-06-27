import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;

public class CsvParser {

  private static final Path PATH_TO_SCV =
      Path.of("/Users/aleksandrvostryakov/projects/my/trading-utils/src/main/resources/stocks.csv");

  public static void main(String[] args) throws Exception {
    var lines = Files.lines(PATH_TO_SCV);

    var items = new ArrayList<Item>();
    var myStock = new HashSet<String>();
    lines
        .skip(1)
        .forEach(
            line -> {
              var item = toItem(line);
              items.add(item);
              myStock.add(item.name);
            });
    for (var name : myStock) {
      var result =
          items.stream()
              .filter(i -> i.name.equals(name))
              .map(Item::calculateResult)
              .reduce(BigDecimal.ZERO, BigDecimal::add);
      System.out.println("name=" + name + "  result=" + result);
    }
  }

  private static Item toItem(String line) {
    var parts = line.split(",");
    var item = new Item();
    item.date = parts[0];
    item.name = parts[1];
    item.count = fromString(parts[2]);
    item.paymentPrice = fromString(parts[3]);
    item.sellPrice = fromString(parts[4]);
    item.commission = fromString(parts[5]);
    if (parts.length == 7) {
      item.dividends = fromString(parts[6]);
    }

    return item;
  }

  private static BigDecimal fromString(String str) {
    if ("".equals(str)) {
      return null;
    }
    try {
      return new BigDecimal(str);
    } catch (Exception ex) {
      System.out.println("ex! " + str);
      return null;
    }
  }

  private static class Item {
    private String date;
    private String name;
    private BigDecimal count;
    private BigDecimal paymentPrice;
    private BigDecimal sellPrice;
    private BigDecimal commission;
    private BigDecimal dividends;

    private BigDecimal calculateResult() {
      if (dividends != null) {
        return dividends;
      }
      if (paymentPrice != null) {
        return paymentPrice.multiply(count).add(commission).negate();
      }
      if (sellPrice != null) {
        return sellPrice.multiply(count).subtract(commission);
      }
      throw new IllegalStateException("item does not contain all data");
    }
  }
}
