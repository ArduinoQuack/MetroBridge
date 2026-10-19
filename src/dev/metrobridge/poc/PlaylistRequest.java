package dev.metrobridge.poc;

public final class PlaylistRequest {
 private PlaylistRequest() {}
 public static String forContainer(String container, boolean shuffled) {
  if (container == null || !container.matches("playlist/[^/\\s]+"))
   throw new IllegalArgumentException("Expected an actual local playlist container ID");
  // MetroList 13.6.1 keeps the trailing empty song selector and falls back to index 0.
  return container + (shuffled ? "/__shuffle__" : "/");
 }
}
