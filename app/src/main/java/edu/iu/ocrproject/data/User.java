package edu.iu.ocrproject.data;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by yapaytech on 19.05.2016.
 */
public class User implements Serializable{
    public String username,name,surname;
    public int age,sex;
    public Set<Integer> likes = new HashSet<>();
    public Set<Integer> dislikes = new HashSet<>();
}
