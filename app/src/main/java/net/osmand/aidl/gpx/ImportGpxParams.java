package net.osmand.aidl.gpx;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import java.io.File;

public class ImportGpxParams implements Parcelable {

	private File gpxFile;
	private Uri gpxUri;
	private String sourceRawData;
	private String destinationPath;

	public ImportGpxParams(File gpxFile, String destinationPath) {
		this.gpxFile = gpxFile;
		this.destinationPath = destinationPath;
	}

	public ImportGpxParams(Uri gpxUri, String destinationPath) {
		this.gpxUri = gpxUri;
		this.destinationPath = destinationPath;
	}

	public ImportGpxParams(String sourceRawData, String destinationPath) {
		this.sourceRawData = sourceRawData;
		this.destinationPath = destinationPath;
	}

	public ImportGpxParams(Parcel in) {
		readFromParcel(in);
	}

	public static final Creator<ImportGpxParams> CREATOR = new
			Creator<ImportGpxParams>() {
				public ImportGpxParams createFromParcel(Parcel in) {
					return new ImportGpxParams(in);
				}

				public ImportGpxParams[] newArray(int size) {
					return new ImportGpxParams[size];
				}
			};

	public File getGpxFile() {
		return gpxFile;
	}

	public Uri getGpxUri() {
		return gpxUri;
	}

	public String getSourceRawData() {
		return sourceRawData;
	}

	public String getDestinationPath() {
		return destinationPath;
	}

	public void writeToParcel(Parcel out, int flags) {
		if (gpxFile != null) {
			out.writeString(gpxFile.getAbsolutePath());
		} else {
			out.writeString(null);
		}
		out.writeParcelable(gpxUri, flags);
		out.writeString(sourceRawData);
		out.writeString(destinationPath);
	}

	private void readFromParcel(Parcel in) {
		String gpxAbsolutePath = in.readString();
		if (gpxAbsolutePath != null) {
			gpxFile = new File(gpxAbsolutePath);
		}
		gpxUri = in.readParcelable(Uri.class.getClassLoader());
		sourceRawData = in.readString();
		destinationPath = in.readString();
	}

	public int describeContents() {
		return 0;
	}
}
