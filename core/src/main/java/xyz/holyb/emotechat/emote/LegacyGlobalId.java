package xyz.holyb.emotechat.emote;

public class LegacyGlobalId {
  public String emoteName;
  public String emoteId;

  public LegacyGlobalId(String emoteName, String emoteId) {
    this.emoteName = emoteName;
    this.emoteId = emoteId;
  }

  private boolean isValid() {
    if (this.emoteName.isEmpty() || this.emoteId.isEmpty()) {
      return false;
    }

    for (char c : this.emoteName.toCharArray()) {
      if (Character.isAlphabetic(c) && !Character.isLowerCase(c)) {
        return false;
      }
    }

    for (char c : this.emoteId.toCharArray()) {
      if (!Character.isUpperCase(c)) {
        return false;
      }
    }

    return true;
  }

  public static LegacyGlobalId parse(String idSplitter, String rawId) {
    if (rawId.isEmpty()) {
      return null;
    }

    if (!idSplitter.isEmpty()) {
      int index = rawId.lastIndexOf(idSplitter);
      if (index == -1) {
        return null;
      }

      LegacyGlobalId id = new LegacyGlobalId(rawId.substring(0, index), rawId.substring(index + 1));
      return id.isValid() ? id : null;
    }

    int splitter = -1;
    boolean foundLower = false;

    char[] chars = rawId.toCharArray();
    for (int i = 0; i < chars.length; i++) {
      char currentChar = chars[i];

      if (Character.isLowerCase(currentChar)) {
        foundLower = true;
      }

      if (Character.isUpperCase(currentChar) && splitter == -1) {
        if (!foundLower) {
          return null;
        }
        splitter = i;
      }

      if ((Character.isLowerCase(currentChar) || !Character.isAlphabetic(currentChar)) && splitter != -1) {
        return null;
      }
    }

    if (splitter == -1) {
      return null;
    }

    return new LegacyGlobalId(rawId.substring(0, splitter), rawId.substring(splitter));
  }

  public String toString(String idSplitter) {
    return this.emoteName + idSplitter + this.emoteId;
  }
}
