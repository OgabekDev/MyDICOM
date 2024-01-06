package dev.ogabek.mydicom.controller;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.UID;
import org.dcm4che3.data.VR;
import org.dcm4che3.io.DicomOutputStream;
import org.dcm4che3.util.UIDUtils;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.Date;

public class Jpg2Dcm {

    public Jpg2Dcm(File file, File fileOutput) {
        try {
            int jpgLen = (int) file.length();

            // Load JPEG image using Android's BitmapFactory
            Bitmap jpegBitmap = BitmapFactory.decodeFile(file.getAbsolutePath());

            if (jpegBitmap == null) {
                throw new Exception("Invalid file.");
            }

            // Create a new dataset (header/metadata) for our DICOM image writer
            Attributes dicom = createDicomHeader(jpegBitmap);

            // Create a new DICOM file meta information dataset
            Attributes fmi = new Attributes();
            fmi.setString(Tag.ImplementationVersionName, VR.SH, "DCM4CHE3");
            fmi.setString(Tag.ImplementationClassUID, VR.UI, UIDUtils.createUID());
            fmi.setString(Tag.TransferSyntaxUID, VR.UI, UID.JPEGLossless);
            fmi.setString(Tag.MediaStorageSOPClassUID, VR.UI, UID.JPEGLossless);
            fmi.setString(Tag.MediaStorageSOPInstanceUID, VR.UI, UIDUtils.createUID());
            fmi.setInt(Tag.FileMetaInformationVersion, VR.OB, 1);
            fmi.setInt(Tag.FileMetaInformationGroupLength, VR.UL, dicom.size() + fmi.size());

            // Create a DICOM output stream for writing the DICOM file
            DicomOutputStream dos = new DicomOutputStream(fileOutput);

            // Write the file meta information and dataset to the DICOM output stream
            dos.writeDataset(fmi, dicom);
            dos.writeHeader(Tag.PixelData, VR.OB, -1);
            dos.writeHeader(Tag.Item, null, 0);
            dos.writeHeader(Tag.Item, null, (jpgLen + 1) & ~1);

            // Read the JPEG file and write its content to the DICOM output stream
            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
            DataInputStream dis = new DataInputStream(bis);
            byte[] buffer = new byte[65536];
            int bytesRead;
            while ((bytesRead = dis.read(buffer)) > 0) {
                dos.write(buffer, 0, bytesRead);
            }

            // Ensure even length and write sequence delimitation item
            if ((jpgLen & 1) != 0) dos.write(0);
            dos.writeHeader(Tag.SequenceDelimitationItem, null, 0);

            // Close the DICOM output stream
            dos.close();

        } catch (Exception e) {
            Log.d("@@@", "Jpg2Dcm: " + e.toString());
            e.printStackTrace();
        }
    }


    private byte[] convertBitmapToBytes(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);  // Assuming JPEG format, adjust as needed
        return stream.toByteArray();
    }

    private Attributes createDicomHeader(Bitmap jpegBitmap) {
        // Determine color components, bits per pixel, bits allocated, and samples per pixel
        int colorComponents = jpegBitmap.getConfig().equals(Bitmap.Config.ARGB_8888) ? 3 : 1;
        int bitsPerPixel = 8 * colorComponents;
        int bitsAllocated = bitsPerPixel;
        int samplesPerPixel = colorComponents;

        // Create a new DICOM dataset (Attributes) for storing information
        Attributes dicom = new Attributes();

        // Add patient related information to the DICOM dataset
        dicom.setString(Tag.PatientBirthName, VR.PN, "James");
        dicom.setString(Tag.PatientName, VR.PN, "Last^First^Middle");
        dicom.setString(Tag.PatientAddress, VR.LO, "Bahor 11");
        dicom.setString(Tag.PatientID, VR.LO, "1234ID");
        dicom.setDate(Tag.PatientBirthDate, VR.DA, new Date()); // Set the actual birth date
        dicom.setString(Tag.PatientSex, VR.CS, "M");

        // Add study related information to the DICOM dataset
        dicom.setString(Tag.AccessionNumber, VR.SH, "1234AC");
        dicom.setString(Tag.StudyID, VR.SH, "1");
        dicom.setString(Tag.StudyDescription, VR.LO, "SINGLEFRAME STUDY");
        dicom.setDate(Tag.StudyDate, VR.DA, new Date());
        dicom.setDate(Tag.StudyTime, VR.TM, new Date());

        // Add series related information to the DICOM dataset
        dicom.setInt(Tag.SeriesNumber, VR.IS, 1);
        dicom.setDate(Tag.SeriesDate, VR.DA, new Date());
        dicom.setDate(Tag.SeriesTime, VR.TM, new Date());
        dicom.setString(Tag.SeriesDescription, VR.LO, "SINGLEFRAME SERIES");
        dicom.setString(Tag.Modality, VR.CS, "SC"); // secondary capture

        // Set various DICOM attributes
        dicom.setString(Tag.SpecificCharacterSet, VR.CS, "ISO_IR 100");
        dicom.setString(Tag.PhotometricInterpretation, VR.CS, samplesPerPixel == 3 ? "YBR_FULL_422" : "MONOCHROME2");
        dicom.setInt(Tag.SamplesPerPixel, VR.US, samplesPerPixel);
        dicom.setInt(Tag.Rows, VR.US, jpegBitmap.getHeight());
        dicom.setInt(Tag.Columns, VR.US, jpegBitmap.getWidth());
        dicom.setInt(Tag.BitsAllocated, VR.US, bitsAllocated);
        dicom.setInt(Tag.BitsStored, VR.US, bitsAllocated);
        dicom.setInt(Tag.HighBit, VR.US, bitsAllocated - 1);
        dicom.setInt(Tag.PixelRepresentation, VR.US, 0);
        dicom.setDate(Tag.InstanceCreationDate, VR.DA, new Date());
        dicom.setDate(Tag.InstanceCreationTime, VR.TM, new Date());

        dicom.setInt(Tag.InstanceNumber, VR.IS, 1);

        // Set unique identifiers for study, series, and instance
        dicom.setString(Tag.SOPClassUID, VR.UI, UID.SecondaryCaptureImageStorage);
        dicom.setString(Tag.StudyInstanceUID, VR.UI, UIDUtils.createUID());
        dicom.setString(Tag.SeriesInstanceUID, VR.UI, UIDUtils.createUID());
        dicom.setString(Tag.SOPInstanceUID, VR.UI, UIDUtils.createUID());

        return dicom;
    }
}