
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import static android.support.v7.widget.RecyclerView.SCROLL_STATE_SETTLING;

/**
 * Created by shaopengxiang on 2018/6/4
 * resolve issue: https://issuetracker.google.com/issues/66996774
 * you may want to try https://gist.github.com/chrisbanes/8391b5adb9ee42180893300850ed02f2  first. it is a better solution!
 * but in some case if you don't want a behavior or you can not have one . you can try this one.
 * must have LinearLayoutManager as layoutmanager! otherwise will be crashed!
 * USEAGE:
 *       .....
 *       recyclerview = new .... ; //your recyclerview instance
 *       RecyclerView66996774Workaround.assistRcyclerView(recyclerview);
 *       .....
 */
public class RecyclerView66996774Workaround {
    private static final int MSG_SCROLL_STATE_IDLE_TIMEOUT = 1;

    /**
     * give it some time wait the fling finish.
     */
    private static final long TIME_TO_FLING_MS = 200;

    private RecyclerView recyclerView;
    private volatile boolean isOnEdge = false;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_SCROLL_STATE_IDLE_TIMEOUT:
                    isOnEdge = false;
                    break;
            }
        }
    };

    public static void assistRcyclerView(RecyclerView recyclerView) {
        new RecyclerView66996774Workaround(recyclerView);
    }

    public RecyclerView66996774Workaround(RecyclerView recyclerView) {
        this.recyclerView = recyclerView;
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (newState == SCROLL_STATE_SETTLING) {
                    if (isOnEdge) {
                        recyclerView.stopScroll();
                        mHandler.removeMessages(MSG_SCROLL_STATE_IDLE_TIMEOUT);
                    }
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
                if (layoutManager != null && layoutManager instanceof LinearLayoutManager) {
                    int last = ((LinearLayoutManager) layoutManager).findLastCompletelyVisibleItemPosition();
                    int first = ((LinearLayoutManager) layoutManager).findFirstCompletelyVisibleItemPosition();
                    RecyclerView.Adapter adapter = recyclerView.getAdapter();
                    if (dy > 0 && last == adapter.getItemCount() - 1) {  // bottom
                        setOnEdge();
                    } else if (dy < 0 && first == 0) {  // top
                        setOnEdge();
                    } else {
                        isOnEdge = false;
                    }
                }
            }
        });
    }

    private void setOnEdge() {
        isOnEdge = true;
        if (recyclerView.getScrollState() == SCROLL_STATE_SETTLING) {
            mHandler.removeMessages(MSG_SCROLL_STATE_IDLE_TIMEOUT);
            recyclerView.stopScroll();
            return;
        }
        mHandler.removeMessages(MSG_SCROLL_STATE_IDLE_TIMEOUT);
        mHandler.sendEmptyMessageDelayed(MSG_SCROLL_STATE_IDLE_TIMEOUT, TIME_TO_FLING_MS);
    }
}
