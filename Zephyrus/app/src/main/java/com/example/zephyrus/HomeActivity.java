package com.example.zephyrus;

import android.os.Bundle;
import com.example.zephyrus.Fragments.MessageFragment;
import com.example.zephyrus.Fragments.GroupFragment;
import com.example.zephyrus.Fragments.FeaturesFragment;
import com.google.android.material.tabs.TabLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import java.util.ArrayList;

public class HomeActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
//        System.out.println("came to home activity");
        Toolbar toolbar=findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");



        TabLayout tabLayout=findViewById(R.id.tab_layout);
        ViewPager viewPager=findViewById(R.id.view_pager);



        ViewPagerAdapter viewPagerAdapter=new ViewPagerAdapter(getSupportFragmentManager());

        viewPagerAdapter.addFragments(new MessageFragment(),"Chats");
        viewPagerAdapter.addFragments(new GroupFragment(),"Groups");
        viewPagerAdapter.addFragments(new FeaturesFragment(),"Features");


        viewPager.setAdapter(viewPagerAdapter);
        tabLayout.setupWithViewPager(viewPager);

    }
}
class ViewPagerAdapter extends FragmentPagerAdapter {


    private ArrayList<Fragment>fragments;
    private ArrayList<String> titles;
    ViewPagerAdapter(FragmentManager fm){
        super(fm,BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        this.fragments=new ArrayList<>();
        this.titles=new ArrayList<>();
    }

    @Override
    public Fragment getItem(int position) {
        return fragments.get(position);
    }

    @Override
    public int getCount() {
        return fragments.size();
    }

    public void addFragments(Fragment fragment,String title){
        fragments.add(fragment);
        titles.add(title);
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return titles.get(position);
    }
}
