package com.q2k.note;

import android.graphics.Canvas;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.q2k.note.MainActivity;
import com.q2k.note.R;

public class SwipeToDeleteCallback extends ItemTouchHelper.SimpleCallback {
    private Drawable deleteIcon;
    private int iconMargin;
    private NoteAdapter noteAdapter;
    private ColorDrawable background;

    SwipeToDeleteCallback(NoteAdapter adapter) {
        super(0, ItemTouchHelper.LEFT);
        deleteIcon = ContextCompat.getDrawable(adapter.getContext(), R.drawable.ic_delete);
        iconMargin = adapter.getContext().getResources().getDimensionPixelSize(R.dimen.icon_margin);
        noteAdapter = adapter;
        background = new ColorDrawable(ContextCompat.getColor(adapter.getContext(), R.color.waring_red));
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
        return false;
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
        int position = viewHolder.getBindingAdapterPosition();
        if (direction == ItemTouchHelper.LEFT) {
            noteAdapter.deleteItem(position);
        }
    }

    @Override
    public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);

        View itemView = viewHolder.itemView;
        int itemHeight = itemView.getHeight();

        // Hiển thị biểu tượng xóa (delete icon) bên phải của mục
        if (dX < 0) {
            int iconTop = itemView.getTop() + (itemHeight - deleteIcon.getIntrinsicHeight()) / 2;
            int iconBottom = iconTop + deleteIcon.getIntrinsicHeight();
            int iconLeft = itemView.getRight() - iconMargin - deleteIcon.getIntrinsicWidth();
            int iconRight = itemView.getRight() - iconMargin;
            background.setBounds(itemView.getLeft(), itemView.getTop(), itemView.getRight(), itemView.getBottom());
            background.draw(c);
            deleteIcon.setBounds(iconLeft, iconTop, iconRight, iconBottom);
            deleteIcon.draw(c);
        } else {
            background.setBounds(0, 0, 0, 0);
        }
    }
}
