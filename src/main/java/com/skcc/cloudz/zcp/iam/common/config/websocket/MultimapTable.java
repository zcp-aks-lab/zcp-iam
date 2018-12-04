package com.skcc.cloudz.zcp.iam.common.config.websocket;

import java.util.List;
import java.util.Map;

import com.google.common.base.Predicates;
import com.google.common.collect.ForwardingTable;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Table;

public class MultimapTable<R, C, V> extends ForwardingTable<R,C,List<V>> {
    private List<V> NULL = ImmutableList.of();

    private Table<R, C, List<V>> del = HashBasedTable.create();

    private List<V> createList() {
        List<V> list = Lists.newArrayList();
        return list;
    }

    public static <R,C,V> MultimapTable<R,C,V> cretae(){
        return new MultimapTable<R,C,V>();
    }

    /*
     * for Forwarding
     */
    @Override
    protected Table<R, C, List<V>> delegate() {
        return del;
    }

    /*
     * for Table
     */
    @Override
    public List<V> get(Object rowKey, Object columnKey) {
        List<V> ret = super.get(rowKey, columnKey);
        return ret == null ? NULL : ret;
    }

    public int indexOf(R rowKey, C columnKey, V value) {
        List<V> list = this.get(rowKey, columnKey);
        return Iterables.indexOf(list, Predicates.equalTo(value));
    }

	public void putValue(R rowKey, C columnKey, V value) {
        List<V> list = super.get(rowKey, columnKey);

        if(list == null || NULL.equals(list)){
            list = createList();
            super.put(rowKey, columnKey, list);
        }

        if(!list.contains(value))
            list.add(value);
    }

	public boolean removeAll(R rowKey, V value) {
        if(!containsRow(rowKey))
            return false;

        boolean removed = false;
        Map<C, List<V>> row = row(rowKey);
        for(C columnKey : row.keySet()) {
            List<V> column = this.get(rowKey, columnKey);
            removed = column.remove(value) || removed;

            //if(column.isEmpty())
            //    this.put(rowKey, columnKey, NULL);
        }
        return removed;
    }
}