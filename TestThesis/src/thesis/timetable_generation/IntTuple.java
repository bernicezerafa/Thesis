package thesis.timetable_generation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;

import com.google.common.primitives.Ints;

public class IntTuple implements Serializable
{
	private static final long serialVersionUID = 1L;
	private final int data[];
	private ArrayList<IntTuple> intTuples = new ArrayList<IntTuple>();
	
    public static IntTuple of(int ... values)
    {
        return new IntTuple(values);
    }

    public IntTuple(int ... data)
    {
        this.data = data;
    }

    public int[] getData()
    {
    	return data;
    }
    
    public int getDimensions()
    {
        return data.length;
    }

    public int get(int index)
    {
    	if (index <= data.length - 1)
    		return data[index];
    	else return -1;
    }
    
    public boolean contains(int exam)
    {
    	for (int i = 0; i < data.length; i++)
    	{
    		if (data[i] == exam)
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
        	intTuples.add(new IntTuple(array));
        }
    }
    
    public boolean isChecked(HashMap<IntTuple, Boolean> alreadyChecked)
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
        return Arrays.toString(data);
    }
    
    @Override
    public int hashCode()
    {
        return Arrays.hashCode(data);
    }
    
    @Override
    public boolean equals(Object object)
    {
        if (this == object)
        {
            return true;
        }
        if (object == null)
        {
            return false;
        }
        if (!(object instanceof IntTuple))
        {
            return false;
        }
        IntTuple other = (IntTuple) object;
        return Arrays.equals(data, other.data);
    }
}