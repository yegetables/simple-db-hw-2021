package simpledb.execution;

import simpledb.common.Database;
import simpledb.common.DbException;
import simpledb.common.Type;
import simpledb.storage.BufferPool;
import simpledb.storage.IntField;
import simpledb.storage.Tuple;
import simpledb.storage.TupleDesc;
import simpledb.transaction.TransactionAbortedException;
import simpledb.transaction.TransactionId;

import java.io.IOException;

/**
 * The delete operator. Delete reads tuples from its child operator and removes
 * them from the table they belong to.
 */
public class Delete extends Operator {

    private TransactionId t;
    private OpIterator child;
    private static final long serialVersionUID = 1L;
    private boolean isDone = false;
    private int count = 0;

    /**
     * Constructor specifying the transaction that this delete belongs to as
     * well as the child to read from.
     * 
     * @param t
     *            The transaction this delete runs in
     * @param child
     *            The child operator from which to read tuples for deletion
     */
    public Delete(TransactionId t, OpIterator child) {
        // some code goes here
        this.t = t;
        this.child = child;
    }

    public TupleDesc getTupleDesc() {
        // some code goes here
        return new TupleDesc(new Type[]{Type.INT_TYPE});
    }

    public void open() throws DbException, TransactionAbortedException {
        // some code goes here
        super.open();
        child.open();
        count = 0;
    }

    public void close() {
        // some code goes here
        super.close();
        child.close();
        count = 0;

    }

    public void rewind() throws DbException, TransactionAbortedException {
        // some code goes here
        child.rewind();
        count = 0;
    }

    /**
     * Deletes tuples as they are read from the child operator. Deletes are
     * processed via the buffer pool (which can be accessed via the
     * Database.getBufferPool() method.
     * 
     * @return A 1-field tuple containing the number of deleted records.
     * @see Database#getBufferPool
     * @see BufferPool#deleteTuple
     */
    protected Tuple fetchNext() throws TransactionAbortedException, DbException {
        // some code goes here


        var td = getTupleDesc();
        var tt = new Tuple(td);
        if (!child.hasNext())
        {
            if (!isDone)
            {
                tt.setField(0, new IntField(count));
                isDone = true;
                return tt;
            }
            else return null;
        }
        var tup = child.next();
        try
        {
            Database.getBufferPool().deleteTuple(t, tup);
            tt.setField(0, new IntField(++count));
            if (!child.hasNext())
            {
                isDone = true;
                return tt;
            }
            else return fetchNext();
        } catch (IOException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public OpIterator[] getChildren() {
        // some code goes here
        return new OpIterator[]{child};
    }

    @Override
    public void setChildren(OpIterator[] children) {
        // some code goes here
        child = children[0];
    }

}
