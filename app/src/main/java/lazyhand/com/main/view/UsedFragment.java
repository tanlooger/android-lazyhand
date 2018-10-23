package lazyhand.com.main.view;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

//import cn.bingoogolapple.badgeview.annotation.BGABadge;
import lazyhand.com.main.R;
import lazyhand.com.main.controller.DevicesController;
import lazyhand.com.main.model.db.DeviceEntity;


import static android.content.ContentValues.TAG;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the
 * interface.

@BGABadge({
        View.class, // 对应 cn.bingoogolapple.badgeview.BGABadgeView，不想用这个类的话就删了这一行
        ImageView.class, // 对应 cn.bingoogolapple.badgeview.BGABadgeImageView，不想用这个类的话就删了这一行
        TextView.class, // 对应 cn.bingoogolapple.badgeview.BGABadgeFloatingTextView，不想用这个类的话就删了这一行
        RadioButton.class, // 对应 cn.bingoogolapple.badgeview.BGABadgeRadioButton，不想用这个类的话就删了这一行
        LinearLayout.class, // 对应 cn.bingoogolapple.badgeview.BGABadgeLinearLayout，不想用这个类的话就删了这一行
        FrameLayout.class, // 对应 cn.bingoogolapple.badgeview.BGABadgeFrameLayout，不想用这个类的话就删了这一行
        RelativeLayout.class, // 对应 cn.bingoogolapple.badgeview.BGABadgeRelativeLayout，不想用这个类的话就删了这一行
        FloatingActionButton.class, // 对应 cn.bingoogolapple.badgeview.BGABadgeFloatingActionButton，不想用这个类的话就删了这一行

})
 */
public class UsedFragment extends Fragment {

    // TODO: Customize parameters

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public UsedFragment() { }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            //mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        RecyclerView recyclerView = (RecyclerView)inflater.inflate(R.layout.fragment_used_list, container, false);

        Context context = recyclerView.getContext();

        recyclerView.setLayoutManager(new LinearLayoutManager(context));

        DevicesController devicesController = ViewModelProviders.of(this).get(DevicesController.class);


        devicesController.getUsedDevicesList().observe(this, (List<DeviceEntity> deviceEntityList)->{
            if(!deviceEntityList.isEmpty()){
                Log.e(TAG, "onCreateView: "+deviceEntityList.get(0).hashcode);

                UsedViewAdapter adapter = new UsedViewAdapter(getActivity(), deviceEntityList, R.layout.fragment_used_list);

                recyclerView.setAdapter(adapter);
            }
        });







        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            /**
             * Callback method to be invoked when the RecyclerView has been scrolled. This will be
             * called after the scroll has completed.
             * This callback will also be called if visible item range changes after a layout
             * calculation. In that case, dx and dy will be 0.
             * @param recyclerView The RecyclerView which scrolled.
             * @param dx           The amount of horizontal scroll.
             * @param dy           The amount of vertical scroll.
             */
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                Log.d(TAG, "onScrolled: ");
                RecyclerView.LayoutManager manager = recyclerView.getLayoutManager();//获取LayoutManager
                //经过测试LinearLayoutManager和GridLayoutManager有以下的方法,这里只针对LinearLayoutManager
                if (manager instanceof LinearLayoutManager) {
                    //经测试第一个完整的可见的item位置，若为0则是最上方那个;在item超过屏幕高度的时候只有第一个item出现的时候为0 ，其他时候会是一个负的值
                    //此方法常用作判断是否能下拉刷新，来解决滑动冲突
                    int findFirstCompletelyVisibleItemPosition = ((LinearLayoutManager) manager).findFirstCompletelyVisibleItemPosition();
                    //最后一个完整的可见的item位置
                    int findLastCompletelyVisibleItemPosition =  ((LinearLayoutManager) manager).findLastCompletelyVisibleItemPosition();
                    //第一个可见的位置
                    int findFirstVisibleItemPosition =  ((LinearLayoutManager) manager).findFirstVisibleItemPosition();
                    //最后一个可见的位置
                    int findLastVisibleItemPosition =  ((LinearLayoutManager) manager).findLastVisibleItemPosition();

                    //如果有滑动冲突--可以用以下方法解决(如果可见位置是position==0的话才能有下拉刷新否则禁掉)
                    recyclerView.setEnabled(findFirstCompletelyVisibleItemPosition==0);
                    //在网上还看到一种解决滑动冲突的方法
                    int topPosition =
                            (recyclerView == null || recyclerView.getChildCount() == 0) ? 0 : recyclerView.getChildAt(0).getTop();
                    Log.e("touch", "onScroll:" + topPosition);

                    recyclerView.setEnabled(topPosition >= 0);


                }
            }
        });

        return recyclerView;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        //mListener = null;
    }



}
