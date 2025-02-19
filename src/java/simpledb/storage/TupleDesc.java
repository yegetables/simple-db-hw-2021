package simpledb.storage;

import simpledb.common.Type;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;

/**
 * TupleDesc describes the schema of a tuple.
 */
public class TupleDesc implements Serializable {

    private ArrayList<TDItem> list = new ArrayList<TDItem>();

    public TupleDesc(ArrayList<Type> types, ArrayList<String> names) {
        this(types.toArray(new Type[0]), names.toArray(new String[0]));
    }

    public ArrayList<Type> getTypes() {
        ArrayList<Type> types = new ArrayList<>();
        for (TDItem item : list)
        {
            types.add(item.fieldType);
        }
        return types;
    }

    public ArrayList<String> getNames() {
        ArrayList<String> names = new ArrayList<>();
        for (TDItem item : list)
        {
            names.add(item.fieldName);
        }
        return names;
    }

    /**
     * A help class to facilitate organizing the information of each field
     */
    public static class TDItem implements Serializable {

        private static final long serialVersionUID = 1L;

        /**
         * The type of the field
         */
        public final Type fieldType;

        /**
         * The name of the field
         */
        public final String fieldName;

        public TDItem(Type t, String n) {
            this.fieldName = n;
            this.fieldType = t;
        }

        public String toString() {
            return fieldName + "(" + fieldType + ")";
        }
    }

    /**
     * @return An iterator which iterates over all the field TDItems
     * that are included in this TupleDesc
     */
    public Iterator<TDItem> iterator() {
        // some code goes here
        return new Iterator<>() {
            final ArrayList<TDItem> lists = list;
            int index = 0;

            @Override
            public boolean hasNext() {
                return index < lists.size();
            }

            @Override
            public TDItem next() {
                return lists.get(index++);
            }
        };
    }

    private static final long serialVersionUID = 1L;

    /**
     * Create a new TupleDesc with typeAr.length fields with fields of the
     * specified types, with associated named fields.
     *
     * @param typeAr  array specifying the number of and types of fields in this
     *                TupleDesc. It must contain at least one entry.
     * @param fieldAr array specifying the names of the fields. Note that names may
     *                be null.
     */
    public TupleDesc(Type[] typeAr, String[] fieldAr) {
        if (typeAr.length != fieldAr.length)
        {
            throw new RuntimeException("typeAr.length != fieldAr.length");
        }
        for (int i = 0; i < typeAr.length; i++)
        {
            list.add(new TDItem(typeAr[i], fieldAr[i]));
        }
        // some code goes here
    }

    /**
     * Constructor. Create a new tuple desc with typeAr.length fields with
     * fields of the specified types, with anonymous (unnamed) fields.
     *
     * @param typeAr array specifying the number of and types of fields in this
     *               TupleDesc. It must contain at least one entry.
     */
    public TupleDesc(Type[] typeAr) {
        for (Type type : typeAr)
        {
            list.add(new TDItem(type, null));
        }
        // some code goes here
    }

    /**
     * @return the number of fields in this TupleDesc
     */
    public int numFields() {
        // some code goes here
        return list.size();
    }

    /**
     * Gets the (possibly null) field name of the ith field of this TupleDesc.
     *
     * @param i index of the field name to return. It must be a valid index.
     * @return the name of the ith field
     * @throws NoSuchElementException if i is not a valid field reference.
     */
    public String getFieldName(int i) throws NoSuchElementException {
        // some code goes here
        if (i < 0 || i >= list.size()) throw new NoSuchElementException();
        return list.get(i).fieldName;
    }

    /**
     * Gets the type of the ith field of this TupleDesc.
     *
     * @param i The index of the field to get the type of. It must be a valid
     *          index.
     * @return the type of the ith field
     * @throws NoSuchElementException if i is not a valid field reference.
     */
    public Type getFieldType(int i) throws NoSuchElementException {
        // some code goes here
        if (i < 0 || i >= list.size()) throw new NoSuchElementException();
        return list.get(i).fieldType;

    }

    /**
     * Find the index of the field with a given name.
     *
     * @param name name of the field.
     * @return the index of the field that is first to have the given name.
     * @throws NoSuchElementException if no field with a matching name is found.
     */
    public int fieldNameToIndex(String name) throws NoSuchElementException {
        // some code goes here
        for (int i = 0; i < list.size(); i++)
        {
            if (Objects.equals(list.get(i).fieldName, name)) return i;
        }
        throw new NoSuchElementException();
    }

    /**
     * @return The size (in bytes) of tuples corresponding to this TupleDesc.
     * Note that tuples from a given TupleDesc are of a fixed size.
     */
    public int getSize() {
        // some code goes here
        int size = 0;
        for (TDItem a : list)
        {
            switch (a.fieldType)
            {
                case INT_TYPE:
                    size += Type.INT_TYPE.getLen();
                    break;
                case STRING_TYPE:
                    size += Type.STRING_TYPE.getLen();
                    break;
                default:
                    size += 0;
                    break;
            }
        }
        return size;
    }

    /**
     * Merge two TupleDescs into one, with td1.numFields + td2.numFields fields,
     * with the first td1.numFields coming from td1 and the remaining from td2.
     *
     * @param td1 The TupleDesc with the first fields of the new TupleDesc
     * @param td2 The TupleDesc with the last fields of the TupleDesc
     * @return the new TupleDesc
     */
    public static TupleDesc merge(TupleDesc td1, TupleDesc td2) {
        // some code goes here
        ArrayList<Type> typeAr = new ArrayList<>(td1.numFields() + td2.numFields());
        ArrayList<String> fieldAr = new ArrayList<>(td1.numFields() + td2.numFields());
        typeAr.addAll(td1.getTypes());
        typeAr.addAll(td2.getTypes());
        fieldAr.addAll(td1.getNames());
        fieldAr.addAll(td2.getNames());
        return new TupleDesc(typeAr.toArray(Type[]::new), fieldAr.toArray(String[]::new));
    }

    /**
     * Compares the specified object with this TupleDesc for equality. Two
     * TupleDescs are considered equal if they have the same number of items
     * and if the i-th type in this TupleDesc is equal to the i-th type in o
     * for every i.
     *
     * @param o the Object to be compared for equality with this TupleDesc.
     * @return true if the object is equal to this TupleDesc.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TupleDesc)) return false;
        TupleDesc tupleDesc = (TupleDesc) o;
        return tupleDesc.getTypes().equals(this.getTypes());
    }

    @Override
    public int hashCode() {
        return getTypes().hashCode();
        // If you want to use TupleDesc as keys for HashMap, implement this so
        // that equal objects have equals hashCode() results
        //        throw new UnsupportedOperationException("unimplemented");
    }

    /**
     * Returns a String describing this descriptor. It should be of the form
     * "fieldType[0](fieldName[0]), ..., fieldType[M](fieldName[M])", although
     * the exact format does not matter.
     *
     * @return String describing this descriptor.
     */
    @Override
    public String toString() {
        return "TupleDesc{" + "list=" + list + '}';
    }
}
