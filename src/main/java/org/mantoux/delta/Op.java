package org.mantoux.delta;

import com.google.gson.JsonObject;

import java.util.Objects;

import static org.mantoux.delta.Op.Type.DELETE;
import static org.mantoux.delta.Op.Type.RETAIN;

public class Op {

  public static Op fromJson(JsonObject op){
    if (op.has("insert")){
      if (op.has("attributes")){
        AttributeMap attributeMap = new AttributeMap();
        JsonObject jsonAttributes = op.get("attributes").getAsJsonObject();
        jsonAttributes.entrySet().forEach((entry) -> attributeMap.put(entry.getKey(), entry.getValue()));
        return Op.insert(op.get("insert").getAsString(), attributeMap);
      }else{
        return Op.insert(op.get("insert").getAsString());
      }
    }else if (op.has("delete")){
      return Op.delete(op.get("delete").getAsInt());
    }else if (op.has("retain")){
      if (op.has("attributes")){
        AttributeMap attributeMap = new AttributeMap();
        JsonObject jsonAttributes = op.get("attributes").getAsJsonObject();
        jsonAttributes.entrySet().forEach((entry) -> attributeMap.put(entry.getKey(), entry.getValue().toString()));
        return Op.retain(op.get("retain").getAsInt(), attributeMap);
      }else{
        return Op.retain(op.get("retain").getAsInt());
      }
    }
    return null;
  }

  // 0 length white space
  static final String EMBED = String.valueOf((char) 0x200b);

  private String  insert;
  private Integer delete;
  private Integer retain;

  private AttributeMap attributes;

  public boolean isDelete() {
    return type().equals(DELETE);
  }

  public boolean isInsert() {
    return type().equals(Type.INSERT);
  }

  public boolean isTextInsert() {
    return isInsert() && !EMBED.equals(insert);
  }

  public boolean isRetain() {
    return type().equals(RETAIN);
  }

  public static Op insert(String arg) {
    return Op.insert(arg, null);
  }

  public static Op insert(String arg, AttributeMap attributes) {
    Op newOp = new Op();
    if (attributes != null && attributes.size() > 0)
      newOp.attributes = attributes;
    newOp.insert = arg;
    return newOp;
  }

  public static Op retain(int length) {
    return Op.retain(length, null);
  }

  public static Op retain(int length, AttributeMap attributes) {
    if (length <= 0)
      throw new IllegalArgumentException("Length should be greater than 0");
    Op newOp = new Op();
    if (attributes != null && attributes.size() > 0)
      newOp.attributes = attributes;
    newOp.retain = length;
    return newOp;
  }

  public static Op delete(int length) {
    if (length <= 0)
      throw new IllegalArgumentException("Length should be greater than 0");
    Op newOp = new Op();
    newOp.delete = length;
    return newOp;
  }

  public Type type() {
    if (insert != null)
      return Type.INSERT;
    if (delete != null)
      return DELETE;
    if (retain != null)
      return RETAIN;
    throw new IllegalStateException("Op has no insert, delete or retain");
  }

  public Op copy() {
    switch (type()) {
      case RETAIN:
        return Op.retain(retain, attributes != null ? attributes.copy() : null);
      case DELETE:
        return Op.delete(delete);
      case INSERT:
        return Op.insert(insert, attributes != null ? attributes.copy() : null);
      default:
        throw new IllegalStateException("Op has no insert, delete or retain");
    }
  }

  public int length() {
    if (type().equals(DELETE))
      return delete;
    if (type().equals(RETAIN))
      return retain;
    return insert.length();
  }

  public AttributeMap attributes() {
    if (type().equals(DELETE))
      return null;
    return attributes != null ? attributes.copy() : null;
  }

  public String arg() {
    if (Type.INSERT.equals(type()))
      return insert;
    throw new UnsupportedOperationException("Only insert op has an argument");
  }

  public String argAsString() {
    return insert;
  }

  public boolean hasAttributes() {
    if (isDelete())
      return false;
    return attributes != null;
  }

  @Override
  public int hashCode() {
    return Objects.hash(insert, delete, retain, attributes);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;
    Op op = (Op) o;
    return Objects.equals(insert, op.insert) && Objects.equals(delete, op.delete) && Objects.equals(
      retain,
      op.retain) && Objects.equals(attributes, op.attributes);
  }

  public JsonObject toJson() {
    JsonObject opObj = new JsonObject();
    if (this.isDelete()){
      opObj.addProperty("delete", this.length());
    }else if (this.isRetain()){
      opObj.addProperty("retain", this.length());
      if (this.hasAttributes()){
        JsonObject attributesObj = new JsonObject();
        AttributeMap attributeMap = this.attributes();
        attributeMap.forEach((key, val) -> attributesObj.addProperty(key, val.toString()));
        opObj.add("attributes", attributesObj);
      }
    }else if (this.isInsert()){
      opObj.addProperty("insert", this.arg());
      if (this.hasAttributes()){
        JsonObject attributesObj = new JsonObject();
        AttributeMap attributeMap = this.attributes();
        attributeMap.forEach((key, val) -> attributesObj.addProperty(key, val.toString()));
        opObj.add("attributes", attributesObj);
      }
    }
    return opObj;
  }

  public enum Type {
    INSERT, DELETE, RETAIN
  }

  static Op retainUntilEnd() {
    return Op.retain(Integer.MAX_VALUE);
  }
}

