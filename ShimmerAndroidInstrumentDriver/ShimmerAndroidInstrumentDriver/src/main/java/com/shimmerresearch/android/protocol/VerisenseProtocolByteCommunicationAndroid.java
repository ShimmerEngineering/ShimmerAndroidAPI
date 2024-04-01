package com.shimmerresearch.android.protocol;

import android.content.Context;
import android.net.Uri;
import android.os.ParcelFileDescriptor;

import androidx.documentfile.provider.DocumentFile;

import com.shimmerresearch.exceptions.ShimmerException;
import com.shimmerresearch.tools.FileUtils;
import com.shimmerresearch.verisense.communication.AbstractByteCommunication;
import com.shimmerresearch.verisense.communication.VerisenseMessage;
import com.shimmerresearch.verisense.communication.VerisenseProtocolByteCommunication;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

public class VerisenseProtocolByteCommunicationAndroid extends VerisenseProtocolByteCommunication {

    protected Context mContext;
    protected Uri mTreeURI;

    public VerisenseProtocolByteCommunicationAndroid(AbstractByteCommunication byteComm) {
        super(byteComm);
    }

    public void enableWriteToBinFile(Context context, Uri treeUri){
        mContext = context;
        mTreeURI = treeUri;
    }

    @Override
    protected void createBinFile(VerisenseMessage verisenseMessage, boolean crcError) {
        System.out.println();
        try {

            String pIndex = String.format("%05d", verisenseMessage.payloadIndex);
            if (crcError) {
                dataFileName = String.format("%s_%s_%s.bin", new SimpleDateFormat("yyMMdd_HHmmss").format(new Date()), pIndex, BadCRC);
            } else {
                dataFileName = String.format("%s_%s.bin", new SimpleDateFormat("yyMMdd_HHmmss").format(new Date()), pIndex);
            }

            // AdvanceLog(LogObject, "BinFileCreated", dataFilePath, ASMName);
        } catch (Exception ex) {
            // AdvanceLog(LogObject, "BinFileCreatedException", ex, ASMName);
        }

    }

    @Override
    public void readLoggedData() throws ShimmerException {
        if (mContext==null || mTreeURI ==null){
            throw new ShimmerException("Context and Uri needs to be set");
        }
       super.readLoggedData();
    }

    protected void WritePayloadToBinFile(VerisenseMessage verisenseMessage) {

        if (PreviouslyWrittenPayloadIndex != verisenseMessage.payloadIndex) {
            try {
                DocumentFile pickedDir = DocumentFile.fromTreeUri(mContext, mTreeURI);
                DocumentFile[] arrDF = pickedDir.listFiles();
                for (DocumentFile file:arrDF) {
                    System.out.println(file.getName());
                }
                //trial name
                DocumentFile dfT = pickedDir.findFile(getTrialName());
                if (dfT==null){
                    dfT = pickedDir.createDirectory(getTrialName());
                }

                //participant name
                DocumentFile dfP = dfT.findFile(getParticipantID());
                if(dfP==null) {
                    dfP = dfT.createDirectory(getParticipantID());
                }

                //uuid
                DocumentFile dfUUID = dfP.findFile(mByteCommunication.getUuid());
                if(dfUUID==null) {
                    dfUUID = dfP.createDirectory(mByteCommunication.getUuid());
                }

                //BinaryFiles
                DocumentFile dfBF = dfUUID.findFile("BinaryFiles");
                if(dfBF==null) {
                    dfBF = dfUUID.createDirectory("BinaryFiles");
                }

                DocumentFile newFile = dfBF.findFile(dataFileName);
                if (newFile == null) {
                    newFile = dfBF.createFile("application/bin", dataFileName);
                    dataFilePath = new FileUtils(mContext).getPath(newFile.getUri(), FileUtils.UriType.FILE);
                }
                if (newFile != null) {
                    ParcelFileDescriptor pfd = mContext.getContentResolver().openFileDescriptor(newFile.getUri(), "wa"); // "w" for write, "a" for append
                    FileOutputStream fos = new FileOutputStream(pfd.getFileDescriptor());
                    fos.write(verisenseMessage.payloadBytes);
                    fos.flush();
                    fos.close();
                }

                /*
                // System.Console.WriteLine("Write Payload To Bin File!");
                File f = new File(dataFilePath);
                if (!f.exists()) {
                    f.createNewFile();
                }
                Files.write(Paths.get(dataFilePath), verisenseMessage.payloadBytes, StandardOpenOption.APPEND);
                */
                if (verisenseMessage.mCRCErrorPayload) {
                    //SaveBinFileToDB();
                } else {
                    // only assume non crc error payload index is valid
                    PreviouslyWrittenPayloadIndex = verisenseMessage.payloadIndex;
                }
                // DataBufferToBeSaved = null;
                // RealmService.UpdateSensorDataSyncDate(Asm_uuid.ToString());
                // UpdateSensorDataSyncDate();


            } catch (Exception ex) {
                // AdvanceLog(LogObject, "FileAppendException", ex, ASMName);
                // throw ex;
                System.out.println(ex.toString());
            }
        } else {
            // AdvanceLog(LogObject, "WritePayloadToBinFile", "Same Payload Index = " +
            // PayloadIndex.ToString(), ASMName);
        }

    }


}
