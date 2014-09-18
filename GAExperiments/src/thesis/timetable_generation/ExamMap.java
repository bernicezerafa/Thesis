package thesis.timetable_generation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;

import com.google.common.primitives.Ints;

public class ExamMap implements Serializable
{
	private static final long serialVersionUID = 1L;
	private final int exams[];
	private ArrayList<ExamMap> intTuples = new ArrayList<ExamMap>();
	
    public static ExamMap getExamRel(int ... values)
    {
        return new ExamMap(values);
    }

    public ExamMap(int ... exams)
    {
        this.exams = exams;
    }

    public int[] getData()
    {
    	return exams;
    }
    
    public int getDimensions()
    {
        return exams.length;
    }

    public int get(int index)
    {
    	if (index <= exams.length - 1)
    		return exams[index];
    	else return -1;
    }
    
    public boolean contains(int exam)
    {
    	for (int i = 0; i < exams.length; i++)
    	{
    		if (exams[i] == exam)
    			return true;
    	}
    	return false;
    }
    
	public void permute(List<Integer> arr, int k)
	{
        for(int i = k; i < arr.size(); i++)
        {
            Collections.swap(arr, i, k);
            permute(arr, k+1);
            Collections.swap(arr, k, i);
        }

        if (k == arr.size() -1)
        {
        	int[] array = ArrayUtils.toPrimitive(arr.toArray(new Integer[arr.size()]));
        	intTuples.add(new ExamMap(array));
        }
    }
    
    public boolean isChecked(HashMap<ExamMap, Boolean> alreadyChecked)
    {
    	List<Integer> array = Ints.asList(this.getData());
    	permute(array, 0);
    	
    	for (int i=0; i < intTuples.size(); i++)
    	{
    		if (!intTuples.get(i).getData().equals(this.getData()))
    		{
    			if (alreadyChecked.get(intTuples.get(i)) != null)
    				return true;
    		}
    	}
    	
    	return false;
    }
	
    @Override
    public String toString()
    {
        return Arrays.toString(exams);
    }
    
    @Override
    public int hashCode()
    {
        return Arrays.hashCode(exams);
    }
    
    @Override
    public boolean equals(Object object)
    {
        if (this == object) return true;
        
        if (object == null)
        {
            return false;
        }
        else
        {	
        	if (object instanceof ExamMap && Arrays.equals(exams, ((ExamMap) object).exams))
        	{
        		return true;
        	}
        }
        return false;
    }
}
