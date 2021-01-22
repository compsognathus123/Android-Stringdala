package htwg.compsognathus.stringdala;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.Log;

import java.util.ArrayList;

public class MandalaPageAdapter extends FragmentStatePagerAdapter
{
    MainActivity main;
    ArrayList<MandalaFragment> fragments;

    public MandalaPageAdapter(FragmentManager fm, MainActivity main)
    {
        super(fm);
        this.main = main;

        fragments = new ArrayList<MandalaFragment>();

        //Insert favorites
        for (Mandala mandala:main.getFavorites())
        {
            MandalaFragment fragment = new MandalaFragment();
            mandala.setFavorite(true);
            fragment.setMandala(mandala);
            fragment.setFavoriteFragment(true);
            addMandalaFragmentListener(fragment);

            fragments.add(fragment);
        }

        //Main fragment for drawing
        addMandalaFragment(main.getMandala());

    }


    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

    private void addMandalaFragmentListener(final MandalaFragment fragment)
    {
        fragment.setMandalaFragmentListener(new MandalaFragment.MandalaFragmentListener() {
            @Override
            public void onMandalaLongClick(Mandala mandala)
            {
                Log.d("longclick", "mod " + mandala.getModulus());
                if(mandala.equals(main.getMandala()))
                {
                    if(main.getMandala().isFavorite())
                    {
                        main.getFavorites().remove(mandala);
                        mandala.setFavorite(false);
                        fragment.setFavoriteFragment(false);
                        fragment.drawView.invalidate();

                    }
                    else {
                        main.getFavorites().add(mandala);
                        fragment.setFavoriteFragment(true);
                        mandala.setFavorite(true);
                        fragment.drawView.invalidate();
                    }

                }else {
                    main.getFavorites().remove(mandala);
                    mandala.setFavorite(false);
                    fragment.setFavoriteFragment(false);
                    fragment.drawView.invalidate();
                    fragments.remove(fragment);

                    notifyDataSetChanged();
                }
                main.saveSharedPreferences();
            }
        });
    }

    public void addMandalaFragment(Mandala mandala)
    {
        MandalaFragment fragment = new MandalaFragment();
        fragment.setMandala(mandala);
        addMandalaFragmentListener(fragment);

        fragments.add(fragment);
        notifyDataSetChanged();
    }

    @Override
    public Fragment getItem(int position)
    {
        return fragments.get(position);
    }

    public MandalaFragment getMandalaFragment(int index)
    {
        return  fragments.get(index);
    }

    public MandalaFragment getMainMandalaFragment()
    {
        return fragments.get(fragments.size() - 1);
    }


    @Override
    public int getCount()
    {
        return fragments.size();
    }
}