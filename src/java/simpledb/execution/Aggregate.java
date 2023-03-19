package simpledb.execution;

import simpledb.common.DbException;
import simpledb.common.Type;
import simpledb.storage.Tuple;
import simpledb.storage.TupleDesc;
import simpledb.transaction.TransactionAbortedException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.NoSuchElementException;

import javax.swing.text.AsyncBoxView.ChildLocator;


/**
 * The Aggregation operator that computes an aggregate (e.g., sum, avg, max,
 * min). Note that we only support aggregates over a single column, grouped by a
 * single column.
 */
public class Aggregate extends Operator {

    private static final long serialVersionUID = 1L;
    OpIterator child;
    int afield;
    int gfield;
    Aggregator.Op aop;
    Aggregator agg;
    OpIterator agg_iter;
    TupleDesc td;

    /**
     * Constructor
     * <p>
     * Implementation hint: depending on the type of afield, you will want to
     * construct an {@link IntegerAggregator} or {@link StringAggregator} to help
     * you with your implementation of readNext().
     *
     * @param child  The OpIterator that is feeding us tuples.
     * @param afield The column over which we are computing an aggregate.
     * @param gfield The column over which we are grouping the result, or -1 if
     *               there is no grouping
     * @param aop    The aggregation operator to use
     */
    public Aggregate(OpIterator child, int afield, int gfield, Aggregator.Op aop) {
        // some code goes here

        /** Code below is to make sure the correct Aggregator is instantiated based on the datatypes of aggretated field and group by field */
        this.child = child;
        this.afield = afield;
        this.gfield = gfield;
        td = child.getTupleDesc();
        this.aop = aop;

        Type gfieldtype;
        //for no grouping, gfield comes in as -1 which is invalid index
        try{
            gfieldtype = td.getFieldType(gfield);
        } catch (NoSuchElementException e){
//            System.out.println("No grouping");
            gfieldtype=null;
        }

        if(td.getFieldType(afield) == Type.INT_TYPE){
            agg = new IntegerAggregator(gfield, gfieldtype , afield, aop);
        }
        else{
            agg = new StringAggregator(gfield, gfieldtype, afield, aop);
        }
        try {
            child.open();   //must open iterator first!
            for(;child.hasNext();){
                agg.mergeTupleIntoGroup(child.next());
            }
        } catch (NoSuchElementException | DbException | TransactionAbortedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return;
        }
        agg_iter = agg.iterator();
    }

    /**
     * @return If this aggregate is accompanied by a groupby, return the groupby
     * field index in the <b>INPUT</b> tuples. If not, return
     * {@link Aggregator#NO_GROUPING}
     */
    public int groupField() {
        // some code goes here
        return gfield;
    }

    /**
     * @return If this aggregate is accompanied by a group by, return the name
     * of the groupby field in the <b>OUTPUT</b> tuples. If not, return
     * null;
     */
    public String groupFieldName() {
        // some code goes here
        try{
            return child.getTupleDesc().getFieldName(groupField());
        }
        catch(Exception e){
            return null;
        }
    }

    /**
     * @return the aggregate field
     */
    public int aggregateField() {
        // some code goes here
        return afield;
    }

    /**
     * @return return the name of the aggregate field in the <b>OUTPUT</b>
     * tuples
     */
    public String aggregateFieldName() {
        // some code goes here
        return child.getTupleDesc().getFieldName(aggregateField());
    }

    /**
     * @return return the aggregate operator
     */
    public Aggregator.Op aggregateOp() {
        // some code goes here
        return aop;
    }

    public static String nameOfAggregatorOp(Aggregator.Op aop) {
        return aop.toString();
    }

    public void open() throws NoSuchElementException, DbException,
            TransactionAbortedException {
        // some code goes here

        super.open();
        agg_iter.open();
    }



    /**
     * Returns the next tuple. If there is a group by field, then the first
     * field is the field by which we are grouping, and the second field is the
     * result of computing the aggregate. If there is no group by field, then
     * the result tuple should contain one field representing the result of the
     * aggregate. Should return null if there are no more tuples.
     */
    protected Tuple fetchNext() throws TransactionAbortedException, DbException {
        // some code goes here
        if (agg_iter.hasNext())
            return agg_iter.next();
        return null;
    }

    public void rewind() throws DbException, TransactionAbortedException {
        // some code goes here
        this.close();
        this.open();
    }

    /**
     * Returns the TupleDesc of this Aggregate. If there is no group by field,
     * this will have one field - the aggregate column. If there is a group by
     * field, the first field will be the group by field, and the second will be
     * the aggregate value column.
     * <p>
     * The name of an aggregate column should be informative. For example:
     * "aggName(aop) (child_td.getFieldName(afield))" where aop and afield are
     * given in the constructor, and child_td is the TupleDesc of the child
     * iterator.
     */
    public TupleDesc getTupleDesc() {
        // some code goes here
        return agg_iter.getTupleDesc();
    }

    public void close() {
        // some code goes here
        super.close();
        agg_iter.close();
    }

    @Override
    public OpIterator[] getChildren() {
        // some code goes here
        return new OpIterator[] {child};
    }

    @Override
    public void setChildren(OpIterator[] children) {
        // some code goes here
        child = children[0];
    }

}
