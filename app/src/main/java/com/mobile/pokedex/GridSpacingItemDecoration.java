package com.mobile.pokedex;

import android.graphics.Rect;
import android.view.View;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class GridSpacingItemDecoration extends RecyclerView.ItemDecoration{
    private final int spacing;

    public GridSpacingItemDecoration(int spacing) {
        this.spacing = spacing;
    }

    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        int position = parent.getChildAdapterPosition(view);
        if (position == RecyclerView.NO_POSITION) {
            return;
        }

        GridLayoutManager layoutManager = (GridLayoutManager) parent.getLayoutManager();
        if (layoutManager == null) {
            return;
        }

        int spanCount = layoutManager.getSpanCount();
        int column = position % spanCount;

        outRect.left = spacing - column * spacing / spanCount;
        outRect.right = (column + 1) * spacing / spanCount;

        if (position < spanCount) {
            outRect.top = spacing;
        }
        outRect.bottom = spacing;
    }
}
