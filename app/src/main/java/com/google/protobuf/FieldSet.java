package com.google.protobuf;

import com.google.protobuf.FieldSet.FieldDescriptorLite;
import com.google.protobuf.Internal;
import com.google.protobuf.LazyField;
import com.google.protobuf.MessageLite;
import com.google.protobuf.WireFormat;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

final class FieldSet<T extends FieldDescriptorLite<T>> {
    private static final int DEFAULT_FIELD_MAP_ARRAY_SIZE = 16;
    private static final FieldSet DEFAULT_INSTANCE = new FieldSet(true);
    final SmallSortedMap<T, Object> fields;
    /* access modifiers changed from: private */
    public boolean hasLazyField;
    private boolean isImmutable;

    public interface FieldDescriptorLite<T extends FieldDescriptorLite<T>> extends Comparable<T> {
        Internal.EnumLiteMap<?> getEnumType();

        WireFormat.JavaType getLiteJavaType();

        WireFormat.FieldType getLiteType();

        int getNumber();

        MessageLite.Builder internalMergeFrom(MessageLite.Builder builder, MessageLite messageLite);

        MutableMessageLite internalMergeFrom(MutableMessageLite mutableMessageLite, MutableMessageLite mutableMessageLite2);

        boolean isPacked();

        boolean isRepeated();
    }

    private FieldSet() {
        this.fields = SmallSortedMap.newFieldMap(16);
    }

    private FieldSet(boolean dummy) {
        this(SmallSortedMap.newFieldMap(0));
        makeImmutable();
    }

    private FieldSet(SmallSortedMap<T, Object> fields2) {
        this.fields = fields2;
        makeImmutable();
    }

    public static <T extends FieldDescriptorLite<T>> FieldSet<T> newFieldSet() {
        return new FieldSet<>();
    }

    public static <T extends FieldDescriptorLite<T>> FieldSet<T> emptySet() {
        return DEFAULT_INSTANCE;
    }

    public static <T extends FieldDescriptorLite<T>> Builder<T> newBuilder() {
        return new Builder<>();
    }

    /* access modifiers changed from: package-private */
    public boolean isEmpty() {
        return this.fields.isEmpty();
    }

    public void makeImmutable() {
        if (!this.isImmutable) {
            this.fields.makeImmutable();
            this.isImmutable = true;
        }
    }

    public boolean isImmutable() {
        return this.isImmutable;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof FieldSet)) {
            return false;
        }
        return this.fields.equals(((FieldSet) o).fields);
    }

    public int hashCode() {
        return this.fields.hashCode();
    }

    public FieldSet<T> clone() {
        FieldSet<T> clone = newFieldSet();
        for (int i = 0; i < this.fields.getNumArrayEntries(); i++) {
            Map.Entry<T, Object> entry = this.fields.getArrayEntryAt(i);
            clone.setField((FieldDescriptorLite) entry.getKey(), entry.getValue());
        }
        for (Map.Entry<T, Object> entry2 : this.fields.getOverflowEntries()) {
            clone.setField((FieldDescriptorLite) entry2.getKey(), entry2.getValue());
        }
        clone.hasLazyField = this.hasLazyField;
        return clone;
    }

    private static <T extends FieldDescriptorLite<T>> Object convertToImmutable(T descriptor, Object value) {
        if (descriptor.getLiteJavaType() == WireFormat.JavaType.MESSAGE) {
            if (descriptor.isRepeated()) {
                List<Object> immutableMessages = new ArrayList<>();
                Iterator it = ((List) value).iterator();
                while (it.hasNext()) {
                    immutableMessages.add(((MutableMessageLite) it.next()).immutableCopy());
                }
                return immutableMessages;
            } else if (value instanceof LazyField) {
                return ((MutableMessageLite) ((LazyField) value).getValue()).immutableCopy();
            } else {
                return ((MutableMessageLite) value).immutableCopy();
            }
        } else if (descriptor.getLiteJavaType() != WireFormat.JavaType.BYTE_STRING) {
            return value;
        } else {
            if (!descriptor.isRepeated()) {
                return ByteString.copyFrom((byte[]) value);
            }
            List<Object> immutableFields = new ArrayList<>();
            Iterator it2 = ((List) value).iterator();
            while (it2.hasNext()) {
                immutableFields.add(ByteString.copyFrom((byte[]) it2.next()));
            }
            return immutableFields;
        }
    }

    private static <T extends FieldDescriptorLite<T>> Object convertToMutable(T descriptor, Object value) {
        if (descriptor.getLiteJavaType() == WireFormat.JavaType.MESSAGE) {
            if (descriptor.isRepeated()) {
                List<Object> mutableMessages = new ArrayList<>();
                Iterator it = ((List) value).iterator();
                while (it.hasNext()) {
                    mutableMessages.add(((MessageLite) it.next()).mutableCopy());
                }
                return mutableMessages;
            } else if (value instanceof LazyField) {
                return ((LazyField) value).getValue().mutableCopy();
            } else {
                return ((MessageLite) value).mutableCopy();
            }
        } else if (descriptor.getLiteJavaType() != WireFormat.JavaType.BYTE_STRING) {
            return value;
        } else {
            if (!descriptor.isRepeated()) {
                return ((ByteString) value).toByteArray();
            }
            List<Object> mutableFields = new ArrayList<>();
            Iterator it2 = ((List) value).iterator();
            while (it2.hasNext()) {
                mutableFields.add(((ByteString) it2.next()).toByteArray());
            }
            return mutableFields;
        }
    }

    public FieldSet<T> cloneWithAllFieldsToImmutable() {
        FieldSet<T> clone = newFieldSet();
        for (int i = 0; i < this.fields.getNumArrayEntries(); i++) {
            Map.Entry<T, Object> entry = this.fields.getArrayEntryAt(i);
            T descriptor = (FieldDescriptorLite) entry.getKey();
            clone.setField(descriptor, convertToImmutable(descriptor, entry.getValue()));
        }
        for (Map.Entry<T, Object> entry2 : this.fields.getOverflowEntries()) {
            T descriptor2 = (FieldDescriptorLite) entry2.getKey();
            clone.setField(descriptor2, convertToImmutable(descriptor2, entry2.getValue()));
        }
        clone.hasLazyField = false;
        return clone;
    }

    public FieldSet<T> cloneWithAllFieldsToMutable() {
        FieldSet<T> clone = newFieldSet();
        for (int i = 0; i < this.fields.getNumArrayEntries(); i++) {
            Map.Entry<T, Object> entry = this.fields.getArrayEntryAt(i);
            T descriptor = (FieldDescriptorLite) entry.getKey();
            clone.setField(descriptor, convertToMutable(descriptor, entry.getValue()));
        }
        for (Map.Entry<T, Object> entry2 : this.fields.getOverflowEntries()) {
            T descriptor2 = (FieldDescriptorLite) entry2.getKey();
            clone.setField(descriptor2, convertToMutable(descriptor2, entry2.getValue()));
        }
        clone.hasLazyField = false;
        return clone;
    }

    public void clear() {
        this.fields.clear();
        this.hasLazyField = false;
    }

    public Map<T, Object> getAllFields() {
        if (!this.hasLazyField) {
            return this.fields.isImmutable() ? this.fields : Collections.unmodifiableMap(this.fields);
        }
        SmallSortedMap<T, Object> result = cloneAllFieldsMap(this.fields, false);
        if (this.fields.isImmutable()) {
            result.makeImmutable();
        }
        return result;
    }

    /* access modifiers changed from: private */
    public static <T extends FieldDescriptorLite<T>> SmallSortedMap<T, Object> cloneAllFieldsMap(SmallSortedMap<T, Object> fields2, boolean copyList) {
        SmallSortedMap<T, Object> result = SmallSortedMap.newFieldMap(16);
        for (int i = 0; i < fields2.getNumArrayEntries(); i++) {
            cloneFieldEntry(result, fields2.getArrayEntryAt(i), copyList);
        }
        for (Map.Entry<T, Object> entry : fields2.getOverflowEntries()) {
            cloneFieldEntry(result, entry, copyList);
        }
        return result;
    }

    private static <T extends FieldDescriptorLite<T>> void cloneFieldEntry(Map<T, Object> map, Map.Entry<T, Object> entry, boolean copyList) {
        T key = (FieldDescriptorLite) entry.getKey();
        Object value = entry.getValue();
        if (value instanceof LazyField) {
            map.put(key, ((LazyField) value).getValue());
        } else if (!copyList || !(value instanceof List)) {
            map.put(key, value);
        } else {
            map.put(key, new ArrayList((List) value));
        }
    }

    public Iterator<Map.Entry<T, Object>> iterator() {
        if (this.hasLazyField) {
            return new LazyField.LazyIterator(this.fields.entrySet().iterator());
        }
        return this.fields.entrySet().iterator();
    }

    /* access modifiers changed from: package-private */
    public Iterator<Map.Entry<T, Object>> descendingIterator() {
        if (this.hasLazyField) {
            return new LazyField.LazyIterator(this.fields.descendingEntrySet().iterator());
        }
        return this.fields.descendingEntrySet().iterator();
    }

    public boolean hasField(T descriptor) {
        if (!descriptor.isRepeated()) {
            return this.fields.get(descriptor) != null;
        }
        throw new IllegalArgumentException("hasField() can only be called on non-repeated fields.");
    }

    public Object getField(T descriptor) {
        Object o = this.fields.get(descriptor);
        if (o instanceof LazyField) {
            return ((LazyField) o).getValue();
        }
        return o;
    }

    /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
     method: com.google.protobuf.SmallSortedMap.put(java.lang.Comparable, java.lang.Object):V
     arg types: [T, java.lang.Object]
     candidates:
      com.google.protobuf.SmallSortedMap.put(java.lang.Object, java.lang.Object):java.lang.Object
      ClspMth{java.util.AbstractMap.put(java.lang.Object, java.lang.Object):V}
      ClspMth{java.util.Map.put(java.lang.Object, java.lang.Object):V}
      com.google.protobuf.SmallSortedMap.put(java.lang.Comparable, java.lang.Object):V */
    public void setField(T descriptor, Object value) {
        if (!descriptor.isRepeated()) {
            verifyType(descriptor.getLiteType(), value);
        } else if (value instanceof List) {
            List<Object> newList = new ArrayList<>();
            newList.addAll((List) value);
            for (Object element : newList) {
                verifyType(descriptor.getLiteType(), element);
            }
            value = newList;
        } else {
            throw new IllegalArgumentException("Wrong object type used with protocol message reflection.");
        }
        if (value instanceof LazyField) {
            this.hasLazyField = true;
        }
        this.fields.put((Comparable) descriptor, value);
    }

    public void clearField(T descriptor) {
        this.fields.remove(descriptor);
        if (this.fields.isEmpty()) {
            this.hasLazyField = false;
        }
    }

    public int getRepeatedFieldCount(T descriptor) {
        if (descriptor.isRepeated()) {
            Object value = getField(descriptor);
            if (value == null) {
                return 0;
            }
            return ((List) value).size();
        }
        throw new IllegalArgumentException("getRepeatedField() can only be called on repeated fields.");
    }

    public Object getRepeatedField(T descriptor, int index) {
        if (descriptor.isRepeated()) {
            Object value = getField(descriptor);
            if (value != null) {
                return ((List) value).get(index);
            }
            throw new IndexOutOfBoundsException();
        }
        throw new IllegalArgumentException("getRepeatedField() can only be called on repeated fields.");
    }

    public void setRepeatedField(T descriptor, int index, Object value) {
        if (descriptor.isRepeated()) {
            Object list = getField(descriptor);
            if (list != null) {
                verifyType(descriptor.getLiteType(), value);
                ((List) list).set(index, value);
                return;
            }
            throw new IndexOutOfBoundsException();
        }
        throw new IllegalArgumentException("getRepeatedField() can only be called on repeated fields.");
    }

    /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
     method: com.google.protobuf.SmallSortedMap.put(java.lang.Comparable, java.lang.Object):V
     arg types: [T, java.util.List<java.lang.Object>]
     candidates:
      com.google.protobuf.SmallSortedMap.put(java.lang.Object, java.lang.Object):java.lang.Object
      ClspMth{java.util.AbstractMap.put(java.lang.Object, java.lang.Object):V}
      ClspMth{java.util.Map.put(java.lang.Object, java.lang.Object):V}
      com.google.protobuf.SmallSortedMap.put(java.lang.Comparable, java.lang.Object):V */
    public void addRepeatedField(T descriptor, Object value) {
        List<Object> list;
        if (descriptor.isRepeated()) {
            verifyType(descriptor.getLiteType(), value);
            Object existingValue = getField(descriptor);
            if (existingValue == null) {
                list = new ArrayList<>();
                this.fields.put((Comparable) descriptor, (Object) list);
            } else {
                list = (List) existingValue;
            }
            list.add(value);
            return;
        }
        throw new IllegalArgumentException("addRepeatedField() can only be called on repeated fields.");
    }

    private void verifyType(WireFormat.FieldType type, Object value) {
        if (!isValidType(type, value)) {
            throw new IllegalArgumentException("Wrong object type used with protocol message reflection.");
        }
    }

    /* access modifiers changed from: private */
    public static boolean isValidType(WireFormat.FieldType type, Object value) {
        Internal.checkNotNull(value);
        switch (type.getJavaType()) {
            case INT:
                return value instanceof Integer;
            case LONG:
                return value instanceof Long;
            case FLOAT:
                return value instanceof Float;
            case DOUBLE:
                return value instanceof Double;
            case BOOLEAN:
                return value instanceof Boolean;
            case STRING:
                return value instanceof String;
            case BYTE_STRING:
                return (value instanceof ByteString) || (value instanceof byte[]);
            case ENUM:
                return (value instanceof Integer) || (value instanceof Internal.EnumLite);
            case MESSAGE:
                return (value instanceof MessageLite) || (value instanceof LazyField);
            default:
                return false;
        }
    }

    public boolean isInitialized() {
        for (int i = 0; i < this.fields.getNumArrayEntries(); i++) {
            if (!isInitialized(this.fields.getArrayEntryAt(i))) {
                return false;
            }
        }
        for (Map.Entry<T, Object> entry : this.fields.getOverflowEntries()) {
            if (!isInitialized(entry)) {
                return false;
            }
        }
        return true;
    }

    /* access modifiers changed from: private */
    public static <T extends FieldDescriptorLite<T>> boolean isInitialized(Map.Entry<T, Object> entry) {
        T descriptor = (FieldDescriptorLite) entry.getKey();
        if (descriptor.getLiteJavaType() == WireFormat.JavaType.MESSAGE) {
            if (descriptor.isRepeated()) {
                for (MessageLite element : (List) entry.getValue()) {
                    if (!element.isInitialized()) {
                        return false;
                    }
                }
            } else {
                Object value = entry.getValue();
                if (value instanceof MessageLite) {
                    return ((MessageLite) value).isInitialized();
                } else if (value instanceof LazyField) {
                    return true;
                } else {
                    throw new IllegalArgumentException("Wrong object type used with protocol message reflection.");
                }
            }
        }
        return true;
    }

    static int getWireFormatForFieldType(WireFormat.FieldType type, boolean isPacked) {
        if (isPacked) {
            return 2;
        }
        return type.getWireType();
    }

    public void mergeFrom(FieldSet<T> other) {
        for (int i = 0; i < other.fields.getNumArrayEntries(); i++) {
            mergeFromField(other.fields.getArrayEntryAt(i));
        }
        for (Map.Entry<T, Object> entry : other.fields.getOverflowEntries()) {
            mergeFromField(entry);
        }
    }

    /* access modifiers changed from: private */
    public static Object cloneIfMutable(Object value) {
        if (value instanceof MutableMessageLite) {
            return ((MutableMessageLite) value).clone();
        }
        if (!(value instanceof byte[])) {
            return value;
        }
        byte[] bytes = (byte[]) value;
        byte[] copy = new byte[bytes.length];
        System.arraycopy(bytes, 0, copy, 0, bytes.length);
        return copy;
    }

    /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
     method: com.google.protobuf.SmallSortedMap.put(java.lang.Comparable, java.lang.Object):V
     arg types: [T, java.lang.Object]
     candidates:
      com.google.protobuf.SmallSortedMap.put(java.lang.Object, java.lang.Object):java.lang.Object
      ClspMth{java.util.AbstractMap.put(java.lang.Object, java.lang.Object):V}
      ClspMth{java.util.Map.put(java.lang.Object, java.lang.Object):V}
      com.google.protobuf.SmallSortedMap.put(java.lang.Comparable, java.lang.Object):V */
    /*  JADX ERROR: JadxRuntimeException in pass: MethodInvokeVisitor
        jadx.core.utils.exceptions.JadxRuntimeException: Not class type: T
        	at jadx.core.dex.info.ClassInfo.checkClassType(ClassInfo.java:60)
        	at jadx.core.dex.info.ClassInfo.fromType(ClassInfo.java:31)
        	at jadx.core.dex.nodes.DexNode.resolveClass(DexNode.java:143)
        	at jadx.core.dex.nodes.RootNode.resolveClass(RootNode.java:183)
        	at jadx.core.dex.nodes.utils.MethodUtils.processMethodArgsOverloaded(MethodUtils.java:75)
        	at jadx.core.dex.nodes.utils.MethodUtils.collectOverloadedMethods(MethodUtils.java:54)
        	at jadx.core.dex.visitors.MethodInvokeVisitor.processOverloaded(MethodInvokeVisitor.java:106)
        	at jadx.core.dex.visitors.MethodInvokeVisitor.processInvoke(MethodInvokeVisitor.java:99)
        	at jadx.core.dex.visitors.MethodInvokeVisitor.processInsn(MethodInvokeVisitor.java:70)
        	at jadx.core.dex.visitors.MethodInvokeVisitor.visit(MethodInvokeVisitor.java:63)
        */
    private void mergeFromField(java.util.Map.Entry<T, java.lang.Object> r8) {
        /*
            r7 = this;
            java.lang.Object r0 = r8.getKey()
            com.google.protobuf.FieldSet$FieldDescriptorLite r0 = (com.google.protobuf.FieldSet.FieldDescriptorLite) r0
            java.lang.Object r1 = r8.getValue()
            boolean r2 = r1 instanceof com.google.protobuf.LazyField
            if (r2 == 0) goto L_0x0015
            r2 = r1
            com.google.protobuf.LazyField r2 = (com.google.protobuf.LazyField) r2
            com.google.protobuf.MessageLite r1 = r2.getValue()
        L_0x0015:
            boolean r2 = r0.isRepeated()
            if (r2 == 0) goto L_0x0049
            java.lang.Object r2 = r7.getField(r0)
            if (r2 != 0) goto L_0x0027
            java.util.ArrayList r3 = new java.util.ArrayList
            r3.<init>()
            r2 = r3
        L_0x0027:
            r3 = r1
            java.util.List r3 = (java.util.List) r3
            java.util.Iterator r3 = r3.iterator()
        L_0x002e:
            boolean r4 = r3.hasNext()
            if (r4 == 0) goto L_0x0043
            java.lang.Object r4 = r3.next()
            r5 = r2
            java.util.List r5 = (java.util.List) r5
            java.lang.Object r6 = cloneIfMutable(r4)
            r5.add(r6)
            goto L_0x002e
        L_0x0043:
            com.google.protobuf.SmallSortedMap<T, java.lang.Object> r3 = r7.fields
            r3.put(r0, r2)
            goto L_0x0091
        L_0x0049:
            com.google.protobuf.WireFormat$JavaType r2 = r0.getLiteJavaType()
            com.google.protobuf.WireFormat$JavaType r3 = com.google.protobuf.WireFormat.JavaType.MESSAGE
            if (r2 != r3) goto L_0x0088
            java.lang.Object r2 = r7.getField(r0)
            if (r2 != 0) goto L_0x0061
            com.google.protobuf.SmallSortedMap<T, java.lang.Object> r3 = r7.fields
            java.lang.Object r4 = cloneIfMutable(r1)
            r3.put(r0, r4)
            goto L_0x0087
        L_0x0061:
            boolean r3 = r2 instanceof com.google.protobuf.MutableMessageLite
            if (r3 == 0) goto L_0x0070
            r3 = r2
            com.google.protobuf.MutableMessageLite r3 = (com.google.protobuf.MutableMessageLite) r3
            r4 = r1
            com.google.protobuf.MutableMessageLite r4 = (com.google.protobuf.MutableMessageLite) r4
            com.google.protobuf.MutableMessageLite r2 = r0.internalMergeFrom(r3, r4)
            goto L_0x0082
        L_0x0070:
            r3 = r2
            com.google.protobuf.MessageLite r3 = (com.google.protobuf.MessageLite) r3
            com.google.protobuf.MessageLite$Builder r3 = r3.toBuilder()
            r4 = r1
            com.google.protobuf.MessageLite r4 = (com.google.protobuf.MessageLite) r4
            com.google.protobuf.MessageLite$Builder r3 = r0.internalMergeFrom(r3, r4)
            com.google.protobuf.MessageLite r2 = r3.build()
        L_0x0082:
            com.google.protobuf.SmallSortedMap<T, java.lang.Object> r3 = r7.fields
            r3.put(r0, r2)
        L_0x0087:
            goto L_0x0091
        L_0x0088:
            com.google.protobuf.SmallSortedMap<T, java.lang.Object> r2 = r7.fields
            java.lang.Object r3 = cloneIfMutable(r1)
            r2.put(r0, r3)
        L_0x0091:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.google.protobuf.FieldSet.mergeFromField(java.util.Map$Entry):void");
    }

    public static Object readPrimitiveField(CodedInputStream input, WireFormat.FieldType type, boolean checkUtf8) throws IOException {
        if (checkUtf8) {
            return WireFormat.readPrimitiveField(input, type, WireFormat.Utf8Validation.STRICT);
        }
        return WireFormat.readPrimitiveField(input, type, WireFormat.Utf8Validation.LOOSE);
    }

    public static Object readPrimitiveFieldForMutable(CodedInputStream input, WireFormat.FieldType type, boolean checkUtf8) throws IOException {
        if (type == WireFormat.FieldType.BYTES) {
            return input.readByteArray();
        }
        return readPrimitiveField(input, type, checkUtf8);
    }

    public void writeTo(CodedOutputStream output) throws IOException {
        for (int i = 0; i < this.fields.getNumArrayEntries(); i++) {
            Map.Entry<T, Object> entry = this.fields.getArrayEntryAt(i);
            writeField((FieldDescriptorLite) entry.getKey(), entry.getValue(), output);
        }
        for (Map.Entry<T, Object> entry2 : this.fields.getOverflowEntries()) {
            writeField((FieldDescriptorLite) entry2.getKey(), entry2.getValue(), output);
        }
    }

    public void writeMessageSetTo(CodedOutputStream output) throws IOException {
        for (int i = 0; i < this.fields.getNumArrayEntries(); i++) {
            writeMessageSetTo(this.fields.getArrayEntryAt(i), output);
        }
        for (Map.Entry<T, Object> entry : this.fields.getOverflowEntries()) {
            writeMessageSetTo(entry, output);
        }
    }

    private void writeMessageSetTo(Map.Entry<T, Object> entry, CodedOutputStream output) throws IOException {
        T descriptor = (FieldDescriptorLite) entry.getKey();
        if (descriptor.getLiteJavaType() != WireFormat.JavaType.MESSAGE || descriptor.isRepeated() || descriptor.isPacked()) {
            writeField(descriptor, entry.getValue(), output);
            return;
        }
        Object value = entry.getValue();
        if (value instanceof LazyField) {
            value = ((LazyField) value).getValue();
        }
        output.writeMessageSetExtension(((FieldDescriptorLite) entry.getKey()).getNumber(), (MessageLite) value);
    }

    static void writeElement(CodedOutputStream output, WireFormat.FieldType type, int number, Object value) throws IOException {
        if (type != WireFormat.FieldType.GROUP) {
            output.writeTag(number, getWireFormatForFieldType(type, false));
            writeElementNoTag(output, type, value);
        } else if (Internal.isProto1Group((MessageLite) value)) {
            output.writeTag(number, 3);
            output.writeGroupNoTag((MessageLite) value);
        } else {
            output.writeGroup(number, (MessageLite) value);
        }
    }

    static void writeElementNoTag(CodedOutputStream output, WireFormat.FieldType type, Object value) throws IOException {
        switch (type) {
            case DOUBLE:
                output.writeDoubleNoTag(((Double) value).doubleValue());
                return;
            case FLOAT:
                output.writeFloatNoTag(((Float) value).floatValue());
                return;
            case INT64:
                output.writeInt64NoTag(((Long) value).longValue());
                return;
            case UINT64:
                output.writeUInt64NoTag(((Long) value).longValue());
                return;
            case INT32:
                output.writeInt32NoTag(((Integer) value).intValue());
                return;
            case FIXED64:
                output.writeFixed64NoTag(((Long) value).longValue());
                return;
            case FIXED32:
                output.writeFixed32NoTag(((Integer) value).intValue());
                return;
            case BOOL:
                output.writeBoolNoTag(((Boolean) value).booleanValue());
                return;
            case GROUP:
                output.writeGroupNoTag((MessageLite) value);
                return;
            case MESSAGE:
                output.writeMessageNoTag((MessageLite) value);
                return;
            case STRING:
                if (value instanceof ByteString) {
                    output.writeBytesNoTag((ByteString) value);
                    return;
                } else {
                    output.writeStringNoTag((String) value);
                    return;
                }
            case BYTES:
                if (value instanceof ByteString) {
                    output.writeBytesNoTag((ByteString) value);
                    return;
                } else {
                    output.writeByteArrayNoTag((byte[]) value);
                    return;
                }
            case UINT32:
                output.writeUInt32NoTag(((Integer) value).intValue());
                return;
            case SFIXED32:
                output.writeSFixed32NoTag(((Integer) value).intValue());
                return;
            case SFIXED64:
                output.writeSFixed64NoTag(((Long) value).longValue());
                return;
            case SINT32:
                output.writeSInt32NoTag(((Integer) value).intValue());
                return;
            case SINT64:
                output.writeSInt64NoTag(((Long) value).longValue());
                return;
            case ENUM:
                if (value instanceof Internal.EnumLite) {
                    output.writeEnumNoTag(((Internal.EnumLite) value).getNumber());
                    return;
                } else {
                    output.writeEnumNoTag(((Integer) value).intValue());
                    return;
                }
            default:
                return;
        }
    }

    public static void writeField(FieldDescriptorLite<?> descriptor, Object value, CodedOutputStream output) throws IOException {
        WireFormat.FieldType type = descriptor.getLiteType();
        int number = descriptor.getNumber();
        if (descriptor.isRepeated()) {
            List<?> valueList = (List) value;
            if (descriptor.isPacked()) {
                output.writeTag(number, 2);
                int dataSize = 0;
                for (Object element : valueList) {
                    dataSize += computeElementSizeNoTag(type, element);
                }
                output.writeRawVarint32(dataSize);
                for (Object element2 : valueList) {
                    writeElementNoTag(output, type, element2);
                }
                return;
            }
            for (Object element3 : valueList) {
                writeElement(output, type, number, element3);
            }
        } else if (value instanceof LazyField) {
            writeElement(output, type, number, ((LazyField) value).getValue());
        } else {
            writeElement(output, type, number, value);
        }
    }

    public int getSerializedSize() {
        int size = 0;
        for (int i = 0; i < this.fields.getNumArrayEntries(); i++) {
            Map.Entry<T, Object> entry = this.fields.getArrayEntryAt(i);
            size += computeFieldSize((FieldDescriptorLite) entry.getKey(), entry.getValue());
        }
        for (Map.Entry<T, Object> entry2 : this.fields.getOverflowEntries()) {
            size += computeFieldSize((FieldDescriptorLite) entry2.getKey(), entry2.getValue());
        }
        return size;
    }

    public int getMessageSetSerializedSize() {
        int size = 0;
        for (int i = 0; i < this.fields.getNumArrayEntries(); i++) {
            size += getMessageSetSerializedSize(this.fields.getArrayEntryAt(i));
        }
        for (Map.Entry<T, Object> entry : this.fields.getOverflowEntries()) {
            size += getMessageSetSerializedSize(entry);
        }
        return size;
    }

    private int getMessageSetSerializedSize(Map.Entry<T, Object> entry) {
        T descriptor = (FieldDescriptorLite) entry.getKey();
        Object value = entry.getValue();
        if (descriptor.getLiteJavaType() != WireFormat.JavaType.MESSAGE || descriptor.isRepeated() || descriptor.isPacked()) {
            return computeFieldSize(descriptor, value);
        }
        if (value instanceof LazyField) {
            return CodedOutputStream.computeLazyFieldMessageSetExtensionSize(((FieldDescriptorLite) entry.getKey()).getNumber(), (LazyField) value);
        }
        return CodedOutputStream.computeMessageSetExtensionSize(((FieldDescriptorLite) entry.getKey()).getNumber(), (MessageLite) value);
    }

    static int computeElementSize(WireFormat.FieldType type, int number, Object value) {
        int tagSize = CodedOutputStream.computeTagSize(number);
        if (type == WireFormat.FieldType.GROUP && !Internal.isProto1Group((MessageLite) value)) {
            tagSize *= 2;
        }
        return computeElementSizeNoTag(type, value) + tagSize;
    }

    static int computeElementSizeNoTag(WireFormat.FieldType type, Object value) {
        switch (type) {
            case DOUBLE:
                return CodedOutputStream.computeDoubleSizeNoTag(((Double) value).doubleValue());
            case FLOAT:
                return CodedOutputStream.computeFloatSizeNoTag(((Float) value).floatValue());
            case INT64:
                return CodedOutputStream.computeInt64SizeNoTag(((Long) value).longValue());
            case UINT64:
                return CodedOutputStream.computeUInt64SizeNoTag(((Long) value).longValue());
            case INT32:
                return CodedOutputStream.computeInt32SizeNoTag(((Integer) value).intValue());
            case FIXED64:
                return CodedOutputStream.computeFixed64SizeNoTag(((Long) value).longValue());
            case FIXED32:
                return CodedOutputStream.computeFixed32SizeNoTag(((Integer) value).intValue());
            case BOOL:
                return CodedOutputStream.computeBoolSizeNoTag(((Boolean) value).booleanValue());
            case GROUP:
                return CodedOutputStream.computeGroupSizeNoTag((MessageLite) value);
            case MESSAGE:
                if (value instanceof LazyField) {
                    return CodedOutputStream.computeLazyFieldSizeNoTag((LazyField) value);
                }
                return CodedOutputStream.computeMessageSizeNoTag((MessageLite) value);
            case STRING:
                if (value instanceof ByteString) {
                    return CodedOutputStream.computeBytesSizeNoTag((ByteString) value);
                }
                return CodedOutputStream.computeStringSizeNoTag((String) value);
            case BYTES:
                if (value instanceof ByteString) {
                    return CodedOutputStream.computeBytesSizeNoTag((ByteString) value);
                }
                return CodedOutputStream.computeByteArraySizeNoTag((byte[]) value);
            case UINT32:
                return CodedOutputStream.computeUInt32SizeNoTag(((Integer) value).intValue());
            case SFIXED32:
                return CodedOutputStream.computeSFixed32SizeNoTag(((Integer) value).intValue());
            case SFIXED64:
                return CodedOutputStream.computeSFixed64SizeNoTag(((Long) value).longValue());
            case SINT32:
                return CodedOutputStream.computeSInt32SizeNoTag(((Integer) value).intValue());
            case SINT64:
                return CodedOutputStream.computeSInt64SizeNoTag(((Long) value).longValue());
            case ENUM:
                if (value instanceof Internal.EnumLite) {
                    return CodedOutputStream.computeEnumSizeNoTag(((Internal.EnumLite) value).getNumber());
                }
                return CodedOutputStream.computeEnumSizeNoTag(((Integer) value).intValue());
            default:
                throw new RuntimeException("There is no way to get here, but the compiler thinks otherwise.");
        }
    }

    public static int computeFieldSize(FieldDescriptorLite<?> descriptor, Object value) {
        WireFormat.FieldType type = descriptor.getLiteType();
        int number = descriptor.getNumber();
        if (!descriptor.isRepeated()) {
            return computeElementSize(type, number, value);
        }
        if (descriptor.isPacked()) {
            int dataSize = 0;
            for (Object element : (List) value) {
                dataSize += computeElementSizeNoTag(type, element);
            }
            return CodedOutputStream.computeTagSize(number) + dataSize + CodedOutputStream.computeRawVarint32Size(dataSize);
        }
        int size = 0;
        for (Object element2 : (List) value) {
            size += computeElementSize(type, number, element2);
        }
        return size;
    }

    static final class Builder<T extends FieldDescriptorLite<T>> {
        private SmallSortedMap<T, Object> fields;
        private boolean hasLazyField;
        private boolean hasNestedBuilders;
        private boolean isMutable;

        private Builder() {
            this(SmallSortedMap.newFieldMap(16));
        }

        private Builder(SmallSortedMap<T, Object> fields2) {
            this.fields = fields2;
            this.isMutable = true;
        }

        public FieldSet<T> build() {
            if (this.fields.isEmpty()) {
                return FieldSet.emptySet();
            }
            this.isMutable = false;
            SmallSortedMap<T, Object> fieldsForBuild = this.fields;
            if (this.hasNestedBuilders) {
                fieldsForBuild = FieldSet.cloneAllFieldsMap(this.fields, false);
                replaceBuilders(fieldsForBuild);
            }
            FieldSet<T> fieldSet = new FieldSet<>(fieldsForBuild);
            boolean unused = fieldSet.hasLazyField = this.hasLazyField;
            return fieldSet;
        }

        private static <T extends FieldDescriptorLite<T>> void replaceBuilders(SmallSortedMap<T, Object> fieldMap) {
            for (int i = 0; i < fieldMap.getNumArrayEntries(); i++) {
                replaceBuilders(fieldMap.getArrayEntryAt(i));
            }
            for (Map.Entry<T, Object> entry : fieldMap.getOverflowEntries()) {
                replaceBuilders(entry);
            }
        }

        private static <T extends FieldDescriptorLite<T>> void replaceBuilders(Map.Entry<T, Object> entry) {
            entry.setValue(replaceBuilders((FieldDescriptorLite) entry.getKey(), entry.getValue()));
        }

        private static <T extends FieldDescriptorLite<T>> Object replaceBuilders(T descriptor, Object value) {
            if (value == null || descriptor.getLiteJavaType() != WireFormat.JavaType.MESSAGE) {
                return value;
            }
            if (!descriptor.isRepeated()) {
                return replaceBuilder(value);
            }
            if (value instanceof List) {
                List<Object> list = (List) value;
                for (int i = 0; i < list.size(); i++) {
                    Object oldElement = list.get(i);
                    Object newElement = replaceBuilder(oldElement);
                    if (newElement != oldElement) {
                        if (list == value) {
                            list = new ArrayList<>(list);
                        }
                        list.set(i, newElement);
                    }
                }
                return list;
            }
            String valueOf = String.valueOf(value.getClass());
            StringBuilder sb = new StringBuilder(valueOf.length() + 66);
            sb.append("Repeated field should contains a List but actually contains type: ");
            sb.append(valueOf);
            throw new IllegalStateException(sb.toString());
        }

        private static Object replaceBuilder(Object value) {
            return value instanceof MessageLite.Builder ? ((MessageLite.Builder) value).build() : value;
        }

        public static <T extends FieldDescriptorLite<T>> Builder<T> fromFieldSet(FieldSet<T> fieldSet) {
            Builder<T> builder = new Builder<>(FieldSet.cloneAllFieldsMap(fieldSet.fields, true));
            builder.hasLazyField = fieldSet.hasLazyField;
            return builder;
        }

        public Map<T, Object> getAllFields() {
            if (!this.hasLazyField) {
                return this.fields.isImmutable() ? this.fields : Collections.unmodifiableMap(this.fields);
            }
            SmallSortedMap<T, Object> result = FieldSet.cloneAllFieldsMap(this.fields, false);
            if (this.fields.isImmutable()) {
                result.makeImmutable();
            } else {
                replaceBuilders(result);
            }
            return result;
        }

        public boolean hasField(T descriptor) {
            if (!descriptor.isRepeated()) {
                return this.fields.get(descriptor) != null;
            }
            throw new IllegalArgumentException("hasField() can only be called on non-repeated fields.");
        }

        public Object getField(T descriptor) {
            return replaceBuilders(descriptor, getFieldAllowBuilders(descriptor));
        }

        /* access modifiers changed from: package-private */
        public Object getFieldAllowBuilders(T descriptor) {
            Object o = this.fields.get(descriptor);
            if (o instanceof LazyField) {
                return ((LazyField) o).getValue();
            }
            return o;
        }

        private void ensureIsMutable() {
            if (!this.isMutable) {
                this.fields = FieldSet.cloneAllFieldsMap(this.fields, true);
                this.isMutable = true;
            }
        }

        /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
         method: com.google.protobuf.SmallSortedMap.put(java.lang.Comparable, java.lang.Object):V
         arg types: [T, java.lang.Object]
         candidates:
          com.google.protobuf.SmallSortedMap.put(java.lang.Object, java.lang.Object):java.lang.Object
          ClspMth{java.util.AbstractMap.put(java.lang.Object, java.lang.Object):V}
          ClspMth{java.util.Map.put(java.lang.Object, java.lang.Object):V}
          com.google.protobuf.SmallSortedMap.put(java.lang.Comparable, java.lang.Object):V */
        public void setField(T descriptor, Object value) {
            ensureIsMutable();
            boolean z = false;
            if (!descriptor.isRepeated()) {
                verifyType(descriptor.getLiteType(), value);
            } else if (value instanceof List) {
                List newList = new ArrayList();
                newList.addAll((List) value);
                for (Object element : newList) {
                    verifyType(descriptor.getLiteType(), element);
                    this.hasNestedBuilders = this.hasNestedBuilders || (element instanceof MessageLite.Builder);
                }
                value = newList;
            } else {
                throw new IllegalArgumentException("Wrong object type used with protocol message reflection.");
            }
            if (value instanceof LazyField) {
                this.hasLazyField = true;
            }
            if (this.hasNestedBuilders || (value instanceof MessageLite.Builder)) {
                z = true;
            }
            this.hasNestedBuilders = z;
            this.fields.put((Comparable) descriptor, value);
        }

        public void clearField(T descriptor) {
            ensureIsMutable();
            this.fields.remove(descriptor);
            if (this.fields.isEmpty()) {
                this.hasLazyField = false;
            }
        }

        public int getRepeatedFieldCount(T descriptor) {
            if (descriptor.isRepeated()) {
                Object value = getField(descriptor);
                if (value == null) {
                    return 0;
                }
                return ((List) value).size();
            }
            throw new IllegalArgumentException("getRepeatedField() can only be called on repeated fields.");
        }

        public Object getRepeatedField(T descriptor, int index) {
            if (this.hasNestedBuilders) {
                ensureIsMutable();
            }
            return replaceBuilder(getRepeatedFieldAllowBuilders(descriptor, index));
        }

        /* access modifiers changed from: package-private */
        public Object getRepeatedFieldAllowBuilders(T descriptor, int index) {
            if (descriptor.isRepeated()) {
                Object value = getFieldAllowBuilders(descriptor);
                if (value != null) {
                    return ((List) value).get(index);
                }
                throw new IndexOutOfBoundsException();
            }
            throw new IllegalArgumentException("getRepeatedField() can only be called on repeated fields.");
        }

        public void setRepeatedField(T descriptor, int index, Object value) {
            ensureIsMutable();
            if (descriptor.isRepeated()) {
                this.hasNestedBuilders = this.hasNestedBuilders || (value instanceof MessageLite.Builder);
                Object list = getField(descriptor);
                if (list != null) {
                    verifyType(descriptor.getLiteType(), value);
                    ((List) list).set(index, value);
                    return;
                }
                throw new IndexOutOfBoundsException();
            }
            throw new IllegalArgumentException("getRepeatedField() can only be called on repeated fields.");
        }

        /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
         method: com.google.protobuf.SmallSortedMap.put(java.lang.Comparable, java.lang.Object):V
         arg types: [T, java.util.List<java.lang.Object>]
         candidates:
          com.google.protobuf.SmallSortedMap.put(java.lang.Object, java.lang.Object):java.lang.Object
          ClspMth{java.util.AbstractMap.put(java.lang.Object, java.lang.Object):V}
          ClspMth{java.util.Map.put(java.lang.Object, java.lang.Object):V}
          com.google.protobuf.SmallSortedMap.put(java.lang.Comparable, java.lang.Object):V */
        public void addRepeatedField(T descriptor, Object value) {
            List<Object> list;
            ensureIsMutable();
            if (descriptor.isRepeated()) {
                this.hasNestedBuilders = this.hasNestedBuilders || (value instanceof MessageLite.Builder);
                verifyType(descriptor.getLiteType(), value);
                Object existingValue = getField(descriptor);
                if (existingValue == null) {
                    list = new ArrayList<>();
                    this.fields.put((Comparable) descriptor, (Object) list);
                } else {
                    list = (List) existingValue;
                }
                list.add(value);
                return;
            }
            throw new IllegalArgumentException("addRepeatedField() can only be called on repeated fields.");
        }

        private static void verifyType(WireFormat.FieldType type, Object value) {
            if (FieldSet.isValidType(type, value)) {
                return;
            }
            if (type.getJavaType() != WireFormat.JavaType.MESSAGE || !(value instanceof MessageLite.Builder)) {
                throw new IllegalArgumentException("Wrong object type used with protocol message reflection.");
            }
        }

        public boolean isInitialized() {
            for (int i = 0; i < this.fields.getNumArrayEntries(); i++) {
                if (!FieldSet.isInitialized(this.fields.getArrayEntryAt(i))) {
                    return false;
                }
            }
            for (Map.Entry<T, Object> entry : this.fields.getOverflowEntries()) {
                if (!FieldSet.isInitialized(entry)) {
                    return false;
                }
            }
            return true;
        }

        public void mergeFrom(FieldSet<T> other) {
            ensureIsMutable();
            for (int i = 0; i < other.fields.getNumArrayEntries(); i++) {
                mergeFromField(other.fields.getArrayEntryAt(i));
            }
            for (Map.Entry<T, Object> entry : other.fields.getOverflowEntries()) {
                mergeFromField(entry);
            }
        }

        /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
         method: com.google.protobuf.SmallSortedMap.put(java.lang.Comparable, java.lang.Object):V
         arg types: [T, java.lang.Object]
         candidates:
          com.google.protobuf.SmallSortedMap.put(java.lang.Object, java.lang.Object):java.lang.Object
          ClspMth{java.util.AbstractMap.put(java.lang.Object, java.lang.Object):V}
          ClspMth{java.util.Map.put(java.lang.Object, java.lang.Object):V}
          com.google.protobuf.SmallSortedMap.put(java.lang.Comparable, java.lang.Object):V */
        /*  JADX ERROR: JadxRuntimeException in pass: MethodInvokeVisitor
            jadx.core.utils.exceptions.JadxRuntimeException: Not class type: T
            	at jadx.core.dex.info.ClassInfo.checkClassType(ClassInfo.java:60)
            	at jadx.core.dex.info.ClassInfo.fromType(ClassInfo.java:31)
            	at jadx.core.dex.nodes.DexNode.resolveClass(DexNode.java:143)
            	at jadx.core.dex.nodes.RootNode.resolveClass(RootNode.java:183)
            	at jadx.core.dex.nodes.utils.MethodUtils.processMethodArgsOverloaded(MethodUtils.java:75)
            	at jadx.core.dex.nodes.utils.MethodUtils.collectOverloadedMethods(MethodUtils.java:54)
            	at jadx.core.dex.visitors.MethodInvokeVisitor.processOverloaded(MethodInvokeVisitor.java:106)
            	at jadx.core.dex.visitors.MethodInvokeVisitor.processInvoke(MethodInvokeVisitor.java:99)
            	at jadx.core.dex.visitors.MethodInvokeVisitor.processInsn(MethodInvokeVisitor.java:70)
            	at jadx.core.dex.visitors.MethodInvokeVisitor.visit(MethodInvokeVisitor.java:63)
            */
        private void mergeFromField(java.util.Map.Entry<T, java.lang.Object> r8) {
            /*
                r7 = this;
                java.lang.Object r0 = r8.getKey()
                com.google.protobuf.FieldSet$FieldDescriptorLite r0 = (com.google.protobuf.FieldSet.FieldDescriptorLite) r0
                java.lang.Object r1 = r8.getValue()
                boolean r2 = r1 instanceof com.google.protobuf.LazyField
                if (r2 == 0) goto L_0x0015
                r2 = r1
                com.google.protobuf.LazyField r2 = (com.google.protobuf.LazyField) r2
                com.google.protobuf.MessageLite r1 = r2.getValue()
            L_0x0015:
                boolean r2 = r0.isRepeated()
                if (r2 == 0) goto L_0x0049
                java.lang.Object r2 = r7.getField(r0)
                if (r2 != 0) goto L_0x0027
                java.util.ArrayList r3 = new java.util.ArrayList
                r3.<init>()
                r2 = r3
            L_0x0027:
                r3 = r1
                java.util.List r3 = (java.util.List) r3
                java.util.Iterator r3 = r3.iterator()
            L_0x002e:
                boolean r4 = r3.hasNext()
                if (r4 == 0) goto L_0x0043
                java.lang.Object r4 = r3.next()
                r5 = r2
                java.util.List r5 = (java.util.List) r5
                java.lang.Object r6 = com.google.protobuf.FieldSet.cloneIfMutable(r4)
                r5.add(r6)
                goto L_0x002e
            L_0x0043:
                com.google.protobuf.SmallSortedMap<T, java.lang.Object> r3 = r7.fields
                r3.put(r0, r2)
                goto L_0x0090
            L_0x0049:
                com.google.protobuf.WireFormat$JavaType r2 = r0.getLiteJavaType()
                com.google.protobuf.WireFormat$JavaType r3 = com.google.protobuf.WireFormat.JavaType.MESSAGE
                if (r2 != r3) goto L_0x0087
                java.lang.Object r2 = r7.getField(r0)
                if (r2 != 0) goto L_0x0061
                com.google.protobuf.SmallSortedMap<T, java.lang.Object> r3 = r7.fields
                java.lang.Object r4 = com.google.protobuf.FieldSet.cloneIfMutable(r1)
                r3.put(r0, r4)
                goto L_0x0086
            L_0x0061:
                boolean r3 = r2 instanceof com.google.protobuf.MessageLite.Builder
                if (r3 == 0) goto L_0x006f
                r3 = r2
                com.google.protobuf.MessageLite$Builder r3 = (com.google.protobuf.MessageLite.Builder) r3
                r4 = r1
                com.google.protobuf.MessageLite r4 = (com.google.protobuf.MessageLite) r4
                r0.internalMergeFrom(r3, r4)
                goto L_0x0086
            L_0x006f:
                r3 = r2
                com.google.protobuf.MessageLite r3 = (com.google.protobuf.MessageLite) r3
                com.google.protobuf.MessageLite$Builder r3 = r3.toBuilder()
                r4 = r1
                com.google.protobuf.MessageLite r4 = (com.google.protobuf.MessageLite) r4
                com.google.protobuf.MessageLite$Builder r3 = r0.internalMergeFrom(r3, r4)
                com.google.protobuf.MessageLite r2 = r3.build()
                com.google.protobuf.SmallSortedMap<T, java.lang.Object> r3 = r7.fields
                r3.put(r0, r2)
            L_0x0086:
                goto L_0x0090
            L_0x0087:
                com.google.protobuf.SmallSortedMap<T, java.lang.Object> r2 = r7.fields
                java.lang.Object r3 = com.google.protobuf.FieldSet.cloneIfMutable(r1)
                r2.put(r0, r3)
            L_0x0090:
                return
            */
            throw new UnsupportedOperationException("Method not decompiled: com.google.protobuf.FieldSet.Builder.mergeFromField(java.util.Map$Entry):void");
        }
    }
}
