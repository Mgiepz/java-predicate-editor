package com.physion.ovation.gui.ebuilder.datamodel;

import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;

import com.physion.ovation.gui.ebuilder.datatypes.ClassDescription;
import com.physion.ovation.gui.ebuilder.datatypes.Attribute;
import com.physion.ovation.gui.ebuilder.datatypes.Type;
import com.physion.ovation.gui.ebuilder.datatypes.Cardinality;


/**
 * 
 * TODO: Perhaps change how the "is null" and "is not null" values are
 * handled.  Right now, I set the attributeOperator to be "is null" or
 * "is not null".  But, I also have to have the last Attribute on the
 * RowData's attributePath be the Attribute.IS_NULL or IS_NOT_NULL value.
 * This seems a bit redundant.  The problem is, I need the Attribute on
 * the path in order to have the comboBox that holds "is null" etc. be created.
 * So, maybe we should not also store that information in the attributeOperator
 * member data?
 */
public class RowData {

    /**
     * This is the "root" row for the whole tree.
     *
     * TODO: I'm not sure if I want to keep this value here.
     */
    private static RowData rootRow;

    /**
     * This is the "topmost", or "root" class that is the ancestor
     * of ALL other rows.
     *
     * Please note this is a static member data that applies to ALL
     * the RowData objects that exist.
     */
    private static ClassDescription classUnderQualification;

    /**
     * This is the class from whose attributes the user
     * will select to create a "path" to this row's childmost attribute.
     * You might also think of this as the "Class Under Qualification"
     * as far as this row is concerned.
     *
     * For example, this might be Epoch or User or Source.
     *
     * TODO: Perhaps come up with a better name?  cuq, entity?
     */
    //private ClassDescription parentClass;

    /**
     * This is the class from whose attributes the user
     * will select to create a "path" to this row's childmost attribute.
     * You might also think of this as the "Class Under Qualification"
     * as far as this row is concerned.
     *
     * For example, this might be Epoch or User or Source.
     *
     * If our parentRow member data is null, then this RowData instance
     * is the "root" row for the whole tree.  I.e. it is the
     * "Class Under Qualification".
     *
     * All rows except the root row have a non-null parentRow.
     * The class of the root row is stored in our static member
     * data classUnderQualification.
     */
    private RowData parentRow;

    /**
     * This is the path to the childmost attribute that
     * this row is specifying.
     *
     * For example, parentClass might be Epoch, and then
     * attributePath could be a list containing the Attributes:
     *
     *      epochGroup  (is of type EpochGroup)
     *      source      (is of type Source)
     *      label       (is of type string)
     *
     * So the above would be specifying the label of the source of
     * the epochGroup of the parentClass.
     *
     * TODO:  Do we want a list of Attribute objects or just Strings.
     */
    private ArrayList<Attribute> attributePath = new ArrayList<Attribute>();

    /**
     * The operator the user selected for this attribute.
     * For example, ==, !=, >=, <=, <, >.
     * Note that "is null" and "is not null" are also considered operators.
     * TODO: Change above comments to be consistent with how I implemented it.
     *
     * Please note, this might be null if this row is a "compound" row
     * that ends with Any, All, or None.
     *
     * We could create enum types to hold these operators, but I think that
     * is a very heavy solution for not much gain.  The user won't be
     * able to enter the values because GUI widgets will be used to
     * select the values, so there isn't any user risk of invalid values
     * being used.  But, there is programmer risk of that.
     * We might change this.
     */
    private String attributeOperator;

    /**
     * If the attributeOperator is set to something AND it is not
     * set to "is null" or "is not null", then this value is the
     * value that the user entered as the desired value for the
     * attribute.
     * TODO: Change above comments to be consistent with how I implemented it.
     *
     * For example, if the attributePath is:  epochGroup.source.label
     * then attributeValue might be something like "Test 27".
     */
    private Object attributeValue;

    /**
     * If the user is specifying a custom "My Property" or "Any Property"
     * attribute, the propName member data will be set to the custom
     * property name that the user entered in the row.
     */
    private String propName;
    private String propType;
    
    /**
     * If this row is a "compound" row, this will be set to
     * Any, All, or None.
     *
     * If this is set to Count, then the
     * attributeOperator member data will be set to some sort of
     * operator such as ==, >, or "is null".
     *
     * If this is null, then this row is an "attribute" row as
     * opposed to a "compound" row.
     */
    private CollectionOperator collectionOperator;

    private ArrayList<RowData> childRows = new ArrayList<RowData>();


    /**
     * Get the number of descendents from this node.  I.e. this is returns
     * the count of our direct children, plus all their children, and all
     * our childrens' children, and so on.
     * This is NOT the same thing as getting the number of this node's children.
     */
    public int getNumDescendents() {

        int count = childRows.size();
        for (RowData childRow : childRows) {
            count += childRow.getNumDescendents();
        }
        return(count);
    }


    /**
     * This returns the RowData object that is at the specified "index".
     * This method is intended to be used to get the RowData object at
     * the specified index as far as a List GUI widget is concerned.
     * This RowData object is at index 0.  Its first child is at index 1.
     * If the first child has a child, then that child is at index 2.
     * If the first child does not have a child, then the second child
     * is at index 2.  (I.e. the first child's sibling.)
     *
     * A simple "picture" of RowData objects and their "indexes" as
     * far as a List widget is concerned, makes this more obvious:
     *
     *      RowData 0
     *          RowData 1
     *          RowData 2
     *          RowData 3
     *               RowData 4
     *          RowData 5
     *               RowData 6
     *               RowData 7
     *               RowData 8
     *          RowData 9
     *
     * So, just to repeat, "this" RowData object is returned if
     * you pass an index of 0.  This RowData's first child is at
     * index 1.
     */
    public RowData getChild(int index) {

        if (index == 0)
            return(this);


        for (RowData childRow : childRows) {

            index--;
            RowData rd = childRow.getChild(index);
            if (rd != null)
                return(rd);

            index -= childRow.getNumDescendents();

            if (index == 0)
                return(childRow);
        }

        return(null);
    }


    /**
     * Get this RowData and all its descendents as an ArrayList of
     * RowData objects.
     */
    public ArrayList<RowData> getRows() {

        int count = getNumDescendents()+1;

        ArrayList<RowData> rows = new ArrayList<RowData>();
        for (int index = 0; index < count; index++)
            rows.add(getChild(index));

        return(rows);
    }


    /**
     * Remove the specified child RowData object from this RowData object's
     * list of direct children.
     */
    public void removeChild(RowData rowData) {
        childRows.remove(rowData);
    }


    /**
     * Remove this RowData object from its parent's list of direct children.
     */
    public void removeFromParent() {

        System.out.println("Removing rowData: "+this.getRowString());
        System.out.println("from parent: "+getParentRow().getRowString());

        getParentRow().getChildRows().remove(this);
    }


    /**
     * Create a child row for this row that is of type Compound Row.
     */
    public void createCompoundRow() {

        System.out.println("createCompoundRow rowData: "+this.getRowString());

        /*
        Attribute attribute = Attribute.SELECT_ATTRIBUTE;
        ArrayList<Attribute> attributePath = new ArrayList<Attribute>();
        attributePath.add(attribute);
        */

        RowData compoundRow = new RowData();
        compoundRow.setParentRow(this);
        //compoundRow.setAttributePath(attributePath);
        compoundRow.setCollectionOperator(CollectionOperator.ANY);

        addChildRow(compoundRow);
    }


    /**
     * Create a child row for this row that is of type Attribute Row.
     */
    public void createAttributeRow() {

        System.out.println("createAttributeRow rowData: "+this.getRowString());

        ClassDescription classDescription = null;

        if ((this != getRootRow()) && isSimpleCompoundRow()) {
            /**
             * TODO:  Not sure I like this mucking around.
             */
            RowData parent = getParentRow();
            while (classDescription == null) {
                Attribute attribute = parent.getChildmostAttribute();
                if (attribute != null)
                    classDescription = attribute.getClassDescription();
                parent = parent.getParentRow();
                if (parent == null)
                    classDescription = getClassUnderQualification();
            }
        }
        else {
            Attribute attribute = getChildmostAttribute();
            if (this == getRootRow())
                classDescription = getClassUnderQualification();
            else
                classDescription = attribute.getClassDescription();
        }


        if (classDescription == null) {
            System.out.println("ERROR: In createAttributeRow "+
                "classDescription == null.  This should never happen.");
            return;
        }

        ArrayList<Attribute> attributes = classDescription.getAllAttributes();
        if (attributes.isEmpty()) {
            System.out.println("ERROR: In createAttributeRow "+
                "attributes.isEmpty == true.  This should never happen.");
            return;
        }

        Attribute attribute = Attribute.SELECT_ATTRIBUTE;
        ArrayList<Attribute> attributePath = new ArrayList<Attribute>();
        attributePath.add(attribute);

        RowData attributeRow = new RowData();
        attributeRow.setAttributePath(attributePath);

        addChildRow(attributeRow);
    }


    /**
     * Returns true if this row is a Compound Row, (simple or not).
     * I.e. this means the row ends with a "compound" Collection Operator
     * Any, All, or None.
     */
    public boolean isCompoundRow() {

        if (collectionOperator == null)
            return(false);

        return(collectionOperator.isCompoundOperator());
    }


    /**
     * Returns true if this is a "simple" Compound Row.
     * I.e. a row that only contains a Collection Operator comboBox.
     */
    public boolean isSimpleCompoundRow() {

        if (isCompoundRow() == false)
            return(false);

        if ((attributePath == null) || attributePath.isEmpty())
            return(true);

        return(false);
    }

    /**
     * Returns true if the user can create child rows under this row.
     */
/*
    public boolean rowCanHaveChildren() {

        if (this == rootRow)
            return(true);

        if (collectionOperator == null)
            return(false);

        return(collectionOperator != CollectionOperator.COUNT);
    }
*/

    /**
     * TODO:  Decide whether I should make this a static method
     * that operates on the "rootRow" member data.
     */
    public /*static*/ void setClassUnderQualification(
        ClassDescription classUnderQualification) {

        RowData.classUnderQualification = classUnderQualification;

        if (parentRow != null) {
            System.out.println(
                "WARNING:  parentRow != null.  Are you confused?");
            parentRow = null;
        }

        if (!childRows.isEmpty()) {
            System.out.println("INFO:  Clearing all childRows.");
            childRows.clear();
        }
    }


    public static ClassDescription getClassUnderQualification() {
        return(classUnderQualification);
    }


    public static RowData getRootRow() {
        return(rootRow);
    }


    public static void setRootRow(RowData rowData) {
        rootRow = rowData;
    }


    public boolean isRootRow() {
        return(this == rootRow);
    }


    /*
    public void setParentClass(ClassDescription parentClass) {
        this.parentClass = parentClass;
    }
    */
    public void setParentRow(RowData parentRow) {
        this.parentRow = parentRow;
    }

    private RowData getParentRow() {
        return(parentRow);
    }


    private ArrayList<RowData> getChildRows() {
        return(childRows);
    }

    public void setCollectionOperator(CollectionOperator collectionOperator) {

        this.collectionOperator = collectionOperator;

        /**
         * Blank out the attribute operator if the user
         * is selecting Any, All, or None.
         */
        if ((collectionOperator != null) &&
            collectionOperator.isCompoundOperator()) {
            setAttributeOperator(null);
            setAttributeValue(null);
        }
    }


    public CollectionOperator getCollectionOperator() {
        return(collectionOperator);
    }


    /**
     * Set the attributeOperator of this row.
     *
     * TODO: Cleanup/reorganize where these assorted operator strings
     * are stored.  Is creating enums overkill?
     *
     * @param attributeOperator - A value such as, but not limited to:
     * ==, !=, >, <, ~=, is null, is not null, is true, is false.
     */
    public void setAttributeOperator(String attributeOperator) {

        System.out.println("Setting attributeOperator to "+attributeOperator);
        this.attributeOperator = attributeOperator;

        /**
         * Blank out the attributeValue if the attributeOperator
         * is being set to the operator "is true", "is false", "is null"
         * "is not null".  (If the operator is set to one of those
         * values, the attributeValue member data is meaningless.)
         *
         * TODO: I don't like the fact that the user *chooses* the
         * "is null" or "is not null" operator using an Attribute comboBox,
         * but we save the operator in the RowData's attributeOperator member
         * data.  That is the way the user sees it in the GUI though.
         */
        if (DataModel.OPERATOR_TRUE.equals(attributeOperator) ||
            DataModel.OPERATOR_FALSE.equals(attributeOperator) ||
            DataModel.OPERATOR_IS_NULL.equals(attributeOperator) ||
            DataModel.OPERATOR_IS_NOT_NULL.equals(attributeOperator)) {
            System.out.println("Setting attributeValue to null");
            setAttributeValue(null);
        }
    }


    public String getAttributeOperator() {
        return(attributeOperator);
    }


    public void setAttributePath(ArrayList<Attribute> attributePath) {
        this.attributePath = attributePath;
    }


    public ArrayList<Attribute> getAttributePath() {
        return(attributePath);
    }


    public void setAttributeValue(Object attributeValue) {

        System.out.print("setAttributeValue("+attributeValue+")");
        /*
        if (attributeValue != null) {
            System.out.println(" class("+attributeValue.getClass()+")");
            if (attributeValue.toString().contains("NZDT") &&
                !(attributeValue instanceof Date)) {
                int x = 1/0;
            }
        }
        */
        this.attributeValue = attributeValue;
    }


    public Object getAttributeValue() {
        return(attributeValue);
    }


    public void setPropName(String propName) {
        this.propName = propName;
    }


    public String getPropName() {
        return(propName);
    }


    public void setPropType(String propType) {

        this.propType = propType;
        if (DataModel.PROP_TYPE_BOOLEAN.equals(propType)) {
            setAttributeOperator(DataModel.OPERATOR_TRUE);
            //setAttributeValue(null);
        }
    }


    public String getPropType() {
        return(propType);
    }


    /**
     * TODO: Do we want to set the parentRow of every child to
     * be this RowData instance?  If so, then we probably want
     * to have an addChildRow() method.
     */
    public void setChildRows(ArrayList<RowData> childRows) {

        this.childRows = childRows;
        for (RowData childRow : childRows)
            childRow.setParentRow(this);
    }


    public void addChildRow(RowData childRow) {

        childRow.setParentRow(this);
        childRows.add(childRow);
    }


    /**
     * Get the number of "levels" that this row is indented.
     */
    public int getIndentCount() {

        int count = 0;
        for (RowData rowData = this.getParentRow(); rowData != null;
             rowData = rowData.getParentRow())
            count++;
        return(count);
    }


    /**
     * Get the amount a RowString should be indented.
     * This method will probably be unused once I switch to using
     * widgets to render a cell.
     */
    public String getIndentString() {

        String indentString = "";
        for (RowData rowData = this.getParentRow(); rowData != null;
             rowData = rowData.getParentRow()) {

            indentString += "      ";
        }
        return(indentString);
    }


    public String toString() {
        return(toString(""));
    }


    public String getRowString() {
        return(getRowString(""));
    }


    /**
     * Get the String representation of JUST THIS row.  I.e. not this
     * row and its children.
     */
    public String getRowString(String indent) {
        
        String string;
        if (getParentClass() != null) 
            string = indent+getParentClass().getName()+" |";
        else 
            string = indent+"ERROR: No Parent Class"+" |";

        /**
         * Do a quick sanity check.
         */
        if (collectionOperator != null &&
            (collectionOperator != CollectionOperator.COUNT) &&
            attributeOperator != null) {
            string += "ERROR: RowData is in an inconsistent state.";
            string += "\ncollectionOperator = "+collectionOperator;
            string += "\nattributeOperator = "+attributeOperator;
            return(string);
        }

        boolean first = true;
        for (Attribute attribute : attributePath) {

            if (attribute != null) {
                if (!attribute.isSpecial()) {
                    /**
                     * Put a dot between each attribute on the path.
                     */
                    if (first) {
                        string += " ";
                        first = false;
                    }
                    else {
                        string += ".";
                    }

                    string += attribute.getName();
                }
            }
            else {
                System.err.println("ERROR:  null Attribute in attributePath.");
                string += "ERROR: attribute == null";
            }
        }

        if (collectionOperator != null) {
            string += " "+collectionOperator;
            //if (!collectionOperator.equals(CollectionOperator.COUNT))
            //    string += " of the following";
        }

        if (propName != null) {
            string += "."+propName;
        }
        if (propType != null) {
            string += "("+propType+")";
        }

        if (attributeOperator != null)
            string += " "+attributeOperator;

        /**
         * Another quick sanity check.
         * If the user specified an attributeOperator other than
         * the "is null" and "is not null" values, then s/he also
         * must also specify an attributeValue.
         */
        /*
        if ((attributeOperator != null) &&
            (!attributeOperator.equals(Attribute.IS_NULL.toString()) &&
             !attributeOperator.equals(Attribute.IS_NOT_NULL.toString())) &&
            (attributeValue == null)) {
            string += "ERROR: RowData is in an inconsistent state.";
            string += "\nattributeOperator = "+attributeOperator;
            string += "\nattributeValue = "+attributeValue;
            return(string);
        }
        */

        /**
         * If the user specified the "is null" or "is not null" operator,
         * then s/he cannot also specify a value.
         */
        if ((attributeOperator != null) &&
            (attributeOperator.equals(Attribute.IS_NULL.toString()) ||
             attributeOperator.equals(Attribute.IS_NOT_NULL.toString())) &&
            (attributeValue != null)) {
            string += "ERROR: RowData is in an inconsistent state.";
            string += "\nattributeOperator = "+attributeOperator;
            string += "\nattributeValue = "+attributeValue;
            return(string);
        }

        if (attributeValue != null) {
            string += " \""+attributeValue+"\"";
        }

        return(string);
    }


    /**
     * Get the "childmost" or "leaf" Attribute that is specified
     * by this row.
     */
    public Attribute getChildmostAttribute() {

        if (getParentRow() == null) {
            /**
             * This is the root row, which does not have an attribute path.
             */
            //return(classUnderQualification);
            //System.out.println("getParentRow() == null");
            return(null);
        }
        else if (attributePath.isEmpty()) {
            System.out.println("attributePath.isEmpty()");
            return(null);
        }
        else
            return(attributePath.get(attributePath.size()-1));
    }


    public ClassDescription getParentClass() {

        if (parentRow == null) {
            //System.out.println("parentRow == null");
            return(classUnderQualification);
        }
        else {
            if (parentRow.getChildmostAttribute() == null) {
                //System.out.println("parentRow.getChildmostAttribute == null");
                return(classUnderQualification);
            }
            else {
                //System.out.println("parentRow.getChildmostAttribute != null");
                //System.out.println("parentRow.getChildmostAttribute().getClassDescription() = "+parentRow.getChildmostAttribute().getClassDescription());
                //System.out.println("parentRow.getChildmostAttribute() = "+parentRow.getChildmostAttribute());
                return(parentRow.getChildmostAttribute().getClassDescription());
            }
        }
    }


    public String toString(String indent) {

        String string = getRowString(indent);

        for (RowData childRow : childRows)
            string += "\n"+childRow.toString(indent+"  ");

        return(string);
    }


    /**
     * This method creates a RowData initialized with a few values
     * for testing purposes.
     */
    public static RowData createTestRowData() {

        ClassDescription entityBaseCD =
            DataModel.getClassDescription("EntityBase");
        ClassDescription taggableEntityBaseCD =
            DataModel.getClassDescription("TaggableEntityBase");
        ClassDescription userCD =
            DataModel.getClassDescription("User");
        ClassDescription keywordTagCD =
            DataModel.getClassDescription("KeywordTag");
        ClassDescription timelineElementCD =
            DataModel.getClassDescription("TimelineElement");
        ClassDescription epochCD =
            DataModel.getClassDescription("Epoch");
        ClassDescription epochGroupCD =
            DataModel.getClassDescription("EpochGroup");
        ClassDescription sourceCD =
            DataModel.getClassDescription("Source");
        ClassDescription resourceCD =
            DataModel.getClassDescription("Resource");
        ClassDescription derivedResponseCD =
            DataModel.getClassDescription("DerivedResponse");

        /**
         * Now create some RowData values.
         */

        /**
         * Create the "root" RowData object.  All of the other
         * rows are children of this row.
         */
        RowData rootRow = new RowData();
        rootRow.setClassUnderQualification(epochCD);
        rootRow.setCollectionOperator(CollectionOperator.ANY);
        RowData.setRootRow(rootRow);

        Attribute attribute;
        ArrayList<Attribute> attributePath;
        RowData rowData;
        ArrayList<RowData> childRows = new ArrayList<RowData>();

        /**
         * Start creating child rows of the rootRow.
         */

        /**
         * Create a row:
         *
         *      epochGroup.source is null
         */

        rowData = new RowData();
        attributePath = new ArrayList<Attribute>();
        attribute = new Attribute("epochGroup", Type.REFERENCE,
                                  epochGroupCD, Cardinality.TO_ONE);
        attributePath.add(attribute);
        attribute = new Attribute("source", Type.REFERENCE,
                                  sourceCD, Cardinality.TO_ONE);
        attributePath.add(attribute);
        attributePath.add(Attribute.IS_NULL);
        rowData.setAttributePath(attributePath);
        childRows.add(rowData);
        rootRow.setChildRows(childRows);


        /**
         * Create a couple "Date/Time" rows.
         */
        rowData = new RowData();
        attributePath = new ArrayList<Attribute>();
        attribute = new Attribute("startTime", Type.DATE_TIME);
        attributePath.add(attribute);
        rowData.setAttributeOperator(">=");
        rowData.setAttributeValue(new GregorianCalendar(2011, 0, 1).getTime());
        rowData.setAttributePath(attributePath);
        childRows.add(rowData);

        rowData = new RowData();
        attributePath = new ArrayList<Attribute>();
        attribute = new Attribute("endTime", Type.DATE_TIME);
        attributePath.add(attribute);
        rowData.setAttributeOperator("<=");
        rowData.setAttributeValue(new Date());
        rowData.setAttributePath(attributePath);
        childRows.add(rowData);

        rootRow.setChildRows(childRows);

        /**
         * Create a "My Property" row.
         */
        rowData = new RowData();
        attributePath = new ArrayList<Attribute>();
        attribute = new Attribute("nextEpoch", Type.REFERENCE,
                                  epochCD, Cardinality.TO_ONE);
        attributePath.add(attribute);
        attributePath.add(Attribute.MY_PROPERTY);

        rowData.setPropName("animalID");
        rowData.setPropType(DataModel.PROP_TYPE_STRING);
        rowData.setAttributeOperator("==");
        rowData.setAttributeValue("x123");
        rowData.setAttributePath(attributePath);

        childRows.add(rowData);
        rootRow.setChildRows(childRows);

        /**
         * Create a "Parameters Map" row.
         */
        rowData = new RowData();
        attributePath = new ArrayList<Attribute>();
        attribute = new Attribute("protocolParameters", Type.PARAMETERS_MAP,
                                  null, Cardinality.N_A);
        attributePath.add(attribute);

        rowData.setPropName("stimulusFrequency");
        rowData.setPropType(DataModel.PROP_TYPE_INT);
        rowData.setAttributeOperator("==");
        rowData.setAttributeValue("5");
        rowData.setAttributePath(attributePath);

        childRows.add(rowData);
        rootRow.setChildRows(childRows);

        /**
         * Create a "Per User" row.
         */
        rowData = new RowData();
        attributePath = new ArrayList<Attribute>();
        attribute = new Attribute("All derivedResponses", Type.PER_USER,
                                  derivedResponseCD, Cardinality.TO_MANY);
        attributePath.add(attribute);

        rowData.setCollectionOperator(CollectionOperator.ALL);
        rowData.setAttributePath(attributePath);

        childRows.add(rowData);
        rootRow.setChildRows(childRows);

        rowData = new RowData();
        attributePath = new ArrayList<Attribute>();
        attribute = new Attribute("epochGroup", Type.REFERENCE,
                                  epochGroupCD, Cardinality.TO_ONE);
        attributePath.add(attribute);
        attribute = new Attribute("source", Type.REFERENCE,
                                  sourceCD, Cardinality.TO_ONE);
        attributePath.add(attribute);
        attribute = new Attribute("label", Type.UTF_8_STRING);
        attributePath.add(attribute);
        rowData.setAttributePath(attributePath);

        rowData.setAttributeOperator("==");
        rowData.setAttributeValue("Test 27");

        childRows.add(rowData);
        rootRow.setChildRows(childRows);

        /**
         * Create another child row.
         */
        rowData = new RowData();
        attributePath = new ArrayList<Attribute>();
        attribute = new Attribute("resources", Type.REFERENCE,
                                  resourceCD, Cardinality.TO_MANY);
        attributePath.add(attribute);
        rowData.setAttributePath(attributePath);
        rowData.setCollectionOperator(CollectionOperator.NONE);

        childRows.add(rowData);
        rootRow.setChildRows(childRows);

        /**
         * Create another child row.
         */
        rowData = new RowData();
        rowData.setCollectionOperator(CollectionOperator.ALL);

        attributePath = new ArrayList<Attribute>();
        attribute = new Attribute("epochGroup", Type.REFERENCE,
                                  epochGroupCD, Cardinality.TO_ONE);
        attributePath.add(attribute);
        attribute = new Attribute("epochs", Type.REFERENCE,
                                  epochCD, Cardinality.TO_MANY);
        attributePath.add(attribute);

        rowData.setAttributePath(attributePath);

        childRows.add(rowData);
        rootRow.setChildRows(childRows);

        RowData rowData2 = new RowData();
        attributePath = new ArrayList<Attribute>();
        attribute = new Attribute("startTime", Type.DATE_TIME);
        attributePath.add(attribute);

        rowData2.setAttributeOperator(">=");
        rowData2.setAttributeValue(new GregorianCalendar(2010, 0, 1).getTime());

        rowData2.setAttributePath(attributePath);
        ArrayList<RowData> childRows2 = new ArrayList<RowData>();
        childRows2.add(rowData2);
        rowData.setChildRows(childRows2);

        /**
         * Create another child row.
         */
/*
        attributePath = new ArrayList<Attribute>();
        attribute = new Attribute("epochGroup", Type.REFERENCE,
                                  epochGroupCD, Cardinality.TO_ONE);
        attributePath.add(attribute);

        attribute = new Attribute("epochs", Type.REFERENCE,
                                  sourceCD, Cardinality.TO_MANY);
        attributePath.add(attribute);

        RowData rowData3 = new RowData();
        rowData3.setAttributePath(attributePath);
        rowData3.setCollectionOperator(CollectionOperator.NONE);

        RowData rowData4 = new RowData();
        attribute = new Attribute("label", Type.UTF_8_STRING);
        attributePath = new ArrayList<Attribute>();
        attributePath.add(attribute);
        rowData4.setAttributePath(attributePath);
        rowData4.setAttributeOperator("==");
        rowData4.setAttributeValue("Test 50");

        ArrayList<RowData> childRows3 = new ArrayList<RowData>();
        childRows3.add(rowData4);
        rowData3.setChildRows(childRows3);

        childRows.add(rowData3);
        rootRow.setChildRows(childRows);
*/

        System.out.println("rootRow:\n"+rootRow.toString());

        return(rootRow);
    }

    
    /**
     * This is a simple test program for this class.
     */
    public static void main(String[] args) {

        System.out.println("RowData test is starting...");

        RowData rootRow = createTestRowData();
        System.out.println(rootRow);

        System.out.println("RowData test is ending.");
    }
}
