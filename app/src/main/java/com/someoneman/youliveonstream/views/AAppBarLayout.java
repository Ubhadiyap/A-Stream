package com.someoneman.youliveonstream.views;

import android.content.Context;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.util.AttributeSet;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ultra on 12.06.2016.
 */
public class AAppBarLayout extends AppBarLayout implements AppBarLayout.OnOffsetChangedListener {

    public enum State {Expended, Collapsed, Idle};

    private State state = State.Expended;
    private ArrayList<OnStateChangeListener> onStateChangeListeners = new ArrayList<>();

    public AAppBarLayout(Context context) {
        super(context);
    }

    public AAppBarLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        if (!(getLayoutParams() instanceof CoordinatorLayout.LayoutParams) || !(getParent() instanceof CoordinatorLayout))
            throw new IllegalStateException("AAppBarLayout must be a direct child of CoordinatorLayout");

        addOnOffsetChangedListener(this);
    }

    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
        if (verticalOffset == 0) {
            if (state != State.Expended) {
                causeListeners(State.Expended);

                state = State.Expended;
            }
        } else if (Math.abs(verticalOffset) >= appBarLayout.getTotalScrollRange()) {
            if (state != State.Collapsed) {
                causeListeners(State.Collapsed);

                state = State.Collapsed;
            }
        } else {
            if (state != State.Idle) {
                causeListeners(State.Idle);

                state = State.Idle;
            }
        }
    }

    public State getState() {
        return state;
    }

    public void addOnStateChangedListener(OnStateChangeListener onStateChangeListener) {
        onStateChangeListeners.add(onStateChangeListener);
    }

    public void removeOnStateChangedListener(OnStateChangeListener onStateChangeListener) {
        onStateChangeListeners.remove(onStateChangeListener);
    }

    private void causeListeners(State state) {
        for (int i = 0; i < onStateChangeListeners.size(); i++) {
            onStateChangeListeners.get(i).onStateChanged(state);
        }
    }

    public interface OnStateChangeListener {
        void onStateChanged(State state);
    }
}
