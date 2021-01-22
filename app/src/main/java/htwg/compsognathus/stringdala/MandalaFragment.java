package htwg.compsognathus.stringdala;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class MandalaFragment extends Fragment {

    DrawView drawView;
    private Mandala mandala;

    private int modulus;
    private int times;
    private boolean isFavoriteFragment;

    MandalaFragmentListener listener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.mandala_fragment, container, false);

        drawView = (DrawView) rootView.findViewById(R.id.MandalaDrawView);

        drawView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v)
            {
                listener.onMandalaLongClick(mandala);
                return false;
            }
        });

        if(mandala != null) drawView.drawMandala(mandala);

        return rootView;
    }

    public void setMandalaFragmentListener(MandalaFragmentListener listener)
    {
        this.listener = listener;
    }

    public interface MandalaFragmentListener
    {
        public void onMandalaLongClick(Mandala mandala);
    }

    public Mandala getMandala()
    {
        return mandala;
    }

    public boolean isFavoriteFragment()
    {
        return isFavoriteFragment;
    }

    public void setFavoriteFragment(boolean favorite)
    {
        this.isFavoriteFragment = favorite;
    }

    public void setMandala(Mandala mandala)
    {
        this.mandala = mandala;

        if(drawView != null)
        {
            drawView.drawMandala(mandala);
        }
        Log.d("mandala settest", "mod " + mandala.getModulus() + " times " + mandala.getTimes());
    }
}