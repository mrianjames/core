package com.oaktree.core.collections.multimap;

import com.oaktree.core.collection.multimap.IMultiMap;
import com.oaktree.core.collection.multimap.MultiMap;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Oaktree Designs Ltd.
 * User: ij
 * Date: 19/01/12
 * Time: 21:48
 */
public class TestMultiMap {

    @Test
    public void testMultimapAdd() {
        IMultiMap<String,String> m = new MultiMap<String,String>();

        m.put("X","Y");
        Assert.assertNotNull(m.get("X"));
        Collection<String> list = m.get("X");
        Assert.assertTrue(list instanceof ArrayList);
        Assert.assertEquals(list.size(),1);
        Assert.assertEquals(list.iterator().next(),"Y");
    }

    @Test
    public void testMultiMapRemove() {
        IMultiMap<String,String> m = new MultiMap<String,String>();
        m.put("X","Y");
        m.clear();
        Assert.assertEquals(m.size(),0);
        Assert.assertNull(m.get("X"));
    }


    @Test
    public void testMultiMapClearKey() {
        IMultiMap<String,String> m = new MultiMap<String,String>();
        m.put("X","Y");
        m.clear("X");
        Assert.assertEquals(m.size(),1);
        Collection<String> list = m.get("X");
        Assert.assertTrue(list instanceof ArrayList);
        Assert.assertEquals(list.size(),0);
    }
}
