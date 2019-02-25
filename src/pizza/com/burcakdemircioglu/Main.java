package pizza.com.burcakdemircioglu;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class Main {

  public static Map<Slice, List<List<Character>>> sliceObjectToSliceMap = new HashMap<>();

  public static void main(String[] args) {
    List<Slice> slices = new ArrayList<>();

    Data data = getData();

    Slice slice = new Slice(0, data.rowCount - 1, 0, data.columnCount - 1);

    while (slice.isSizeValid(data)) {
//      System.out.println("slice: " + slice);

      List<List<Character>> newSlice = sliceObjectToSliceMap.get(slice);
      if (newSlice == null) {
        newSlice = recurse(slices, data, slice);
      }
//      slice.x1=newSlice.size();

//      System.out.println(new StringJoiner(", ")
//                           .add("newSlice=" + newSlice)
//                           .toString());
      if (newSlice.isEmpty()) {
        break;
      }
      slice.y1 += newSlice.get(0).size();
    }
    System.out.println("sliceCount: " + slices.size());
    System.out.println(new StringJoiner(", ")
                         .add("slices=" + slices)
                         .toString());

    System.out.println(new StringJoiner(", ")
                         .add("visited=" + data.visited)
                         .toString());

  }

  public static List<List<Character>> recurse(List<Slice> slices, Data data, Slice slice) {

//    System.out.println("recurse slice: " + slice);

    if (!slice.isSizeValid(data)) {
      return new ArrayList<>();
    }

    if (isValid(data, slice)) {
      slices.add(slice);
      updateVisited(data, slice);
      return getSliceMatrix(data, slice);
    }

    Slice newSliceLeft = new Slice(slice.x1, slice.x2, slice.y1, slice.y2 - 1);
    List<List<Character>> newSlice = sliceObjectToSliceMap.get(newSliceLeft);
    if (newSlice == null) {
      newSlice = recurse(slices, data, newSliceLeft);//kolon kesme
      sliceObjectToSliceMap.put(slice, newSlice);
    }

    if (newSlice.isEmpty()) {
      Slice newSliceUp = new Slice(slice.x1, slice.x2 - 1, slice.y1, slice.y2);
      newSlice = sliceObjectToSliceMap.get(newSliceUp);
      if (newSlice == null) {
        newSlice = recurse(slices, data, new Slice(slice.x1, slice.x2 - 1, slice.y1, slice.y2));//row kesme
        sliceObjectToSliceMap.put(slice, newSlice);
      }
    }

    return newSlice;
  }

  public static void updateVisited(Data data, Slice slice) {
    for (int i = slice.x1; i <= slice.x2; i++) {
      for (int j = slice.y1; j <= slice.y2; j++) {
        data.visited.get(i).set(j, true);
      }
    }
  }

  public static boolean isValid(Data data, Slice slice) {
    if (slice.getArea() > data.maxCount) {
      return false;
    }
    if (slice.getArea() < data.minCount * 2) {
      return false;
    }

    List<List<Character>> sliceMatrix = getSliceMatrix(data, slice);

//    System.out.println(new StringJoiner(", ")
//      .add("matrix=" + slice)
//      .toString());

    List<Character> flatSlice = sliceMatrix.stream()
                                           .flatMap(Collection::stream)
                                           .collect(Collectors.toList());

    long tomatoCount = flatSlice.stream()
                                .filter(c -> c == 'T')
                                .count();

    long mushroomCount = flatSlice.stream()
                                  .filter(c -> c == 'M')
                                  .count();

    if (tomatoCount < data.minCount || mushroomCount < data.minCount) {
      return false;
    }

    List<List<Boolean>> sliceVisited = getSliceVisited(data, slice);

//    System.out.println(new StringJoiner(", ")
//      .add("matrix=" + slice)
//      .toString());

    List<Boolean> flatSliceVisited = sliceVisited.stream()
                                                 .flatMap(Collection::stream)
                                                 .collect(Collectors.toList());
    return !flatSliceVisited.contains(true);
//    return true;
  }

  private static List<List<Character>> getSliceMatrix(Data data, Slice slice) {
    return data.matrix.stream()
                      .skip(slice.x1)
                      .limit(slice.x2 - slice.x1 + 1)
                      .map(row -> row.stream()
                                     .skip(slice.y1)
                                     .limit(slice.y2 - slice.y1 + 1)
                                     .collect(Collectors.toList()))
                      .collect(Collectors.toList());
  }

  private static List<List<Boolean>> getSliceVisited(Data data, Slice slice) {
    return data.visited.stream()
                       .skip(slice.x1)
                       .limit(slice.x2 - slice.x1 + 1)
                       .map(row -> row.stream()
                                      .skip(slice.y1)
                                      .limit(slice.y2 - slice.y1 + 1)
                                      .collect(Collectors.toList()))
                       .collect(Collectors.toList());
  }

  public static Data getData() {
    String filePath = new File("").getAbsolutePath();

    try (BufferedReader br = new BufferedReader(new FileReader(filePath + "/c_medium.in"))) {

      String sCurrentLine = br.readLine();

      String[] nmArray = sCurrentLine.split(" ");
      int rowCount = Integer.valueOf(nmArray[0]);
      int columnCount = Integer.valueOf(nmArray[1]);
      int minCount = Integer.valueOf(nmArray[2]);
      int maxCount = Integer.valueOf(nmArray[3]);

      List<List<Character>> matrix = new ArrayList<>();
      List<List<Boolean>> visitedMatrix = new ArrayList<>();

      for (int i = 0; i < rowCount; i++) {
        List<Character> list = new ArrayList<>();
        List<Boolean> visitedList = new ArrayList<>();

        sCurrentLine = br.readLine();
        String[] itemArray = sCurrentLine.split("");

        for (int j = 0; j < itemArray.length; j++) {
          list.add(itemArray[j].charAt(0));
          visitedList.add(false);
        }
        matrix.add(list);
        visitedMatrix.add(visitedList);
      }

      return new Data(rowCount, columnCount, minCount, maxCount, matrix, visitedMatrix);

    } catch (IOException e) {
      e.printStackTrace();
    }

    return null;
  }

  public static class Data {

    public int rowCount;
    public int columnCount;
    public int minCount;
    public int maxCount;
    public List<List<Character>> matrix;
    public List<List<Boolean>> visited;

    public Data(int rowCount, int columnCount, int minCount, int maxCount, List<List<Character>> matrix,
      List<List<Boolean>> visited) {
      this.rowCount = rowCount;
      this.columnCount = columnCount;
      this.minCount = minCount;
      this.maxCount = maxCount;
      this.matrix = matrix;
      this.visited = visited;
    }

    @Override
    public String toString() {
      return new StringJoiner(", ", Data.class.getSimpleName() + "[", "]")
        .add("rowCount=" + rowCount)
        .add("columnCount=" + columnCount)
        .add("minCount=" + minCount)
        .add("maxCount=" + maxCount)
        .add("matrix=" + matrix)
        .add("visited=" + visited)
        .toString();
    }

  }

  public static class Slice {

    public int x1, x2, y1, y2;

    public Slice(int x1, int x2, int y1, int y2) {
      this.x1 = x1;
      this.x2 = x2;
      this.y1 = y1;
      this.y2 = y2;
    }

    public boolean isSizeValid(Data data) {
      if (y2 < y1 || x2 < x1) {
        return false;
      }
      if (getArea() < data.minCount * 2) {
        return false;
      }
      return true;
    }

    public int getArea() {
      return (y2 - y1 + 1) * (x2 - x1 + 1);
    }

    @Override
    public String toString() {
      return new StringJoiner(", ", Slice.class.getSimpleName() + "[", "]")
        .add("x1=" + x1)
        .add("x2=" + x2)
        .add("y1=" + y1)
        .add("y2=" + y2)
        .toString();
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }

      Slice slice = (Slice) o;

      if (x1 != slice.x1) {
        return false;
      }
      if (x2 != slice.x2) {
        return false;
      }
      if (y1 != slice.y1) {
        return false;
      }
      return y2 == slice.y2;
    }

    @Override
    public int hashCode() {
      int result = x1;
      result = 31 * result + x2;
      result = 31 * result + y1;
      result = 31 * result + y2;
      return result;
    }

  }

}
