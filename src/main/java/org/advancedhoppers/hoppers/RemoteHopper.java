package org.advancedhoppers.hoppers;


public class RemoteHopper {
//    private static RemoteHopper instance;
//    private static File remoteHopperDir = AdvancedHoppers.getHoppersDir("remoteHopper");
//    private File hoppersDir = remoteHopperDir.toPath().resolve("hoppers").toFile();
//    private File codeLocationIndexDir = remoteHopperDir.toPath().resolve("codeLocationIndex").toFile();
//    private File codeChunkIndexDir = remoteHopperDir.toPath().resolve("codeChunkIndex").toFile();
//    private Set<String> codeSet = new HashSet<>();
//
//    private String getCodeByLocation(int x, int y, int z, String worldName){
//        String filename = x + "_" + y + "_" + z + "_" + worldName + ".json";
//        File file = new File(codeLocationIndexDir, filename);
//        if (!file.exists()){
//            return null;
//        }
//        try {
//            return Files.toString(file, StandardCharsets.UTF_8);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return null;
//    }
//
//    private String getCodeByChunk(Chunk chunk){
//        int chunkX = chunk.getX();
//        int chunkZ = chunk.getZ();
//        String worldName = chunk.getWorld().getName();
//        String filename = chunkX + "_" + chunkZ + "_" + worldName + ".json";
//        File file = new File(codeChunkIndexDir, filename);
//        if (!file.exists()){
//            return null;
//        }
//        try {
//            return Files.toString(file, StandardCharsets.UTF_8);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return null;
//    }
//
//    private class Receiver {
//        public Receiver(Location location, String code) {
//        }
//    }
//
//    Receiver constructReceiver(Location location, String code){
//        if (hoppersDir.toPath().resolve("R_"+code+".json").toFile().exists()){
//            return null;
//        }
//        return new Receiver(location, code);
//    }
//
//    private class Sender {
//        public Sender(Location location, String code) {
//
//        }
//
//    }
//
//    Sender constructSender(Location location, String code){
//        if (hoppersDir.toPath().resolve("S_"+code+".json").toFile().exists()){
//            return null;
//        }
//        return new Sender(location, code);
//    }
//
//    public void saveCodeSet(){
//        JsonObject jsonObject = new JsonObject();
//        JsonArray jsonArray = new JsonArray();
//        for (String i : codeSet){
//            jsonArray.add(i);
//        }
//        jsonObject.add("codeSet", jsonArray);
//        try {
//            FileWriter fileWriter = new FileWriter(new File(remoteHopperDir, "codeSet.json"));
//            fileWriter.write(jsonObject.toString());
//            fileWriter.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    public RemoteHopper() {
//        instance = this;
//        File codeSet = new File(remoteHopperDir, "codeSet.json");
//
//        if (codeSet.exists()){
//            try {
//                JsonObject jsonObject = new Gson().fromJson(new FileReader(codeSet), JsonObject.class);
//                JsonArray jsonArray = jsonObject.getAsJsonArray("codeSet");
//                for (int i = 0; i < jsonArray.size(); i++){
//                    this.codeSet.add(jsonArray.get(i).getAsString());
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//
//
//
//
//    }
}
