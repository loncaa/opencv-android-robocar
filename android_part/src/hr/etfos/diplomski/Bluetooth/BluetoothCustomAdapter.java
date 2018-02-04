package hr.etfos.diplomski.Bluetooth;

import hr.etfos.diplomski.R;
import hr.etfos.diplomski.R.id;

import java.util.List;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

/* Base adapter nema implementirane funkcije kao sto su add() */
public class BluetoothCustomAdapter extends ArrayAdapter<BluetoothDevice> {

	private int resourceId;
	private int selectedPosition = 0;
	private boolean isPopulated = false;
	
	private BluetoothDevice device;
	
	public BluetoothCustomAdapter(Context context, int resource, List<BluetoothDevice> devices) {
		super(context, resource, devices);
		this.resourceId = resource;	 //
		
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if(convertView == null)
		{
			isPopulated = true;
			convertView = View.inflate(super.getContext(), resourceId, null);
		}
		
		RadioButton rbutton = (RadioButton) convertView.findViewById(R.id.rButton);	
		TextView deviceName = (TextView) convertView.findViewById(R.id.device_name);
		TextView deviceAddres = (TextView) convertView.findViewById(R.id.device_adress);
		
		device = super.getItem(position);
		
		//oznaci selektricani radio-button, za pocetak je to prvi radio-button
		rbutton.setChecked(position == selectedPosition); //kad je netki drugi oznacen, on ce ove sve druge postaviti u false
		rbutton.setTag(position);
		
		
		rbutton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				selectedPosition = (Integer) v.getTag();
				Toast.makeText(getContext(), "You selected " + device.getName() + "\n" + selectedPosition, Toast.LENGTH_SHORT).show();
				notifyDataSetChanged();	
			}
		});
		
		deviceName.setText(device.getName());
		deviceAddres.setText(device.getAddress());
		
		return convertView;
	};
	
	public int getPositionOfSelectedRadioButton()
	{
		if(isPopulated)
			return selectedPosition;
		else 
			return -1;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}
}
