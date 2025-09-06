package org;

import java.util.HashMap;
import java.util.Map;

public enum HeaderType {
  USERNAME('U'),
  JOIN('J'),
  MESSAGE('M'),
  GET('G'),
  EXIT('E');

  private final char firstChar;
  private static final Map<Character, HeaderType> CHAR_TO_TYPE = new HashMap<>();

  static {
    for (HeaderType type : HeaderType.values()) {
      CHAR_TO_TYPE.put(type.firstChar, type);
    }
  }

  HeaderType(char firstChar) {
    this.firstChar = firstChar;
  }

  public static HeaderType matchFirstCharacter(char c) {
    return CHAR_TO_TYPE.get(c);
  }
}
