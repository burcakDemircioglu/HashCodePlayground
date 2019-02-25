package streaming.com.burcakdemircioglu;

import java.io.*;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

public class Main {

  public static void main(String[] args) {

    Map<Integer, Integer> cacheLoad = new HashMap<>();//cacheId->totalLaod
    Map<Integer, Set<Integer>> cacheResult = new HashMap<>();//cacheId->videoIds

    Data data = getData();

    data.endpoints.forEach(endPoint -> {
      if (!endPoint.caches.isEmpty()) {
        endPoint.requests.forEach(request -> {

          Set<Integer> flatCacheResult = endPoint.caches.stream()
                                                        .flatMap(cache -> {
                                                          Set<Integer> integers = cacheResult.get(cache.id);
                                                          if (integers == null) {
                                                            return new HashSet<Integer>().stream();
                                                          }
                                                          return integers.stream();
                                                        })
                                                        .collect(Collectors.toSet());

          if (!flatCacheResult.contains(request.videoId)) {
            for (int cacheId = 0; cacheId < endPoint.caches.size(); cacheId++) {

              Integer oldLoad = cacheLoad.get(cacheId);
              if (oldLoad == null) {
                oldLoad = 0;
              }
              int newLoad = oldLoad + data.videoSize.get(request.videoId);
              if (newLoad <= data.cacheMemory) {
                cacheLoad.put(cacheId, newLoad);
                Set<Integer> cachedVideos = cacheResult.get(cacheId);
                if (cachedVideos == null) {
                  cachedVideos = new HashSet<>();
                }
                cachedVideos.add(request.videoId);
                cacheResult.put(cacheId, cachedVideos);

                cacheId = endPoint.caches.size();
              }
            }
          }

        });
      }
    });

    writeUsingFileWriter(cacheResult);
  }

  private static void writeUsingFileWriter(Map<Integer, Set<Integer>> cacheResult) {

    cacheResult = cacheResult.entrySet().stream()
                             .filter(entry -> !entry.getValue().isEmpty())
                             .collect(Collectors.toMap(Entry::getKey, Entry::getValue));

    String data;
    String filePath = new File("").getAbsolutePath();

    File file = new File(filePath + "/src/streaming/com/out.txt");
    FileWriter fr = null;
    try {
      fr = new FileWriter(file);
      fr.write(cacheResult.size() + "\n");

      FileWriter finalFr = fr;
      List<Entry<Integer, Set<Integer>>> cacheResultList = cacheResult.entrySet().stream()
                                                                      .sorted(Comparator.comparingInt(Entry::getKey))
                                                                      .collect(Collectors.toList());

      for (Entry<Integer, Set<Integer>> entry : cacheResultList) {

        finalFr.write(entry.getKey() + " ");
        String join = entry.getValue().stream()
                           .map(video -> video + "")
                           .collect(Collectors.joining(" "));
        finalFr.write(join + "\n");
      }

    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      //close resources
      try {
        fr.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  public static Data getData() {
    String filePath = new File("").getAbsolutePath();

    try (BufferedReader br = new BufferedReader(new FileReader(filePath + "/src/streaming/com/me_at_the_zoo.in"))) {

      String sCurrentLine = br.readLine();

      String[] nmArray = sCurrentLine.split(" ");
      int videoCount = Integer.valueOf(nmArray[0]);
      int endpointCount = Integer.valueOf(nmArray[1]);
      int requestDescriptionCount = Integer.valueOf(nmArray[2]);
      int cacheCount = Integer.valueOf(nmArray[3]);
      int cacheMemory = Integer.valueOf(nmArray[4]);

      sCurrentLine = br.readLine();

      Data data = new Data();
      data.cacheMemory = cacheMemory;
      data.cacheCount = cacheCount;

      Arrays.stream(sCurrentLine.split(" ")).forEach(video -> {
        data.videoSize.add(Integer.valueOf(video));
      });

      for (int i = 0; i < endpointCount; i++) {
        sCurrentLine = br.readLine();
        String[] endPointArray = sCurrentLine.split(" ");
        EndPoint endPoint = new EndPoint();
        endPoint.id = i;
        endPoint.dataCenterLatency = Integer.valueOf(endPointArray[0]);
        int connectedCashCount = Integer.valueOf(endPointArray[1]);

        for (int j = 0; j < connectedCashCount; j++) {
          sCurrentLine = br.readLine();
          String[] cacheLatencyArray = sCurrentLine.split(" ");
          int id = Integer.valueOf(cacheLatencyArray[0]);
          int latency = Integer.valueOf(cacheLatencyArray[1]);
          endPoint.caches.add(new Cache(id, latency));
        }
        endPoint.caches.sort(Comparator.comparingInt(cache -> cache.latency));
        data.endpoints.add(endPoint);
      }

      for (int i = 0; i < requestDescriptionCount; i++) {
        sCurrentLine = br.readLine();
        String[] descriptionArray = sCurrentLine.split(" ");

        int videoId = Integer.valueOf(descriptionArray[0]);
        int endpointId = Integer.valueOf(descriptionArray[1]);
        int requestAmount = Integer.valueOf(descriptionArray[2]);
        data.endpoints.get(endpointId).requests.add(new Request(videoId, requestAmount));
      }
      data.endpoints.forEach(endPoint -> endPoint.requests.sort(Comparator.comparingInt(request -> -request.amount)));

      data.endpoints.sort(Comparator.comparingInt(endpoint -> -endpoint.requests.size()));

      return data;

    } catch (IOException e) {
      e.printStackTrace();
    }

    return null;
  }

  public static class Data {

    public List<EndPoint> endpoints = new ArrayList<>();
    public List<Integer> videoSize = new ArrayList<>();
    public int cacheMemory;
    public int cacheCount;

  }

  public static class EndPoint {

    public int id;
    public List<Request> requests = new ArrayList<>();
    public Integer dataCenterLatency;
    public List<Cache> caches = new ArrayList<>();

  }

  public static class Request {

    public int videoId;
    public int amount;

    public Request(int videoId, int amount) {
      this.videoId = videoId;
      this.amount = amount;
    }

  }

  public static class Cache {

    public int id;
    public int latency;

    public Cache(int id, int latency) {
      this.id = id;
      this.latency = latency;
    }

  }

}
