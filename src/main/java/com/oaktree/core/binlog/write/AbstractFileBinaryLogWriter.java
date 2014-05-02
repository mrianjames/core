package com.oaktree.core.binlog.write;

import com.oaktree.core.pool.IObjectFactory;
import com.oaktree.core.pool.IPool;
import com.oaktree.core.pool.SimplePool;
import com.oaktree.core.utils.ByteUtils;
import com.oaktree.core.utils.UnsafeMemory;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Mechanics
 *
 * Created with IntelliJ IDEA.
 * User: IJ
 * Date: 09/04/13
 * Time: 18:35
 * To change this template use File | Settings | File Templates.
 */
public class AbstractFileBinaryLogWriter extends AbstractBinaryLogWriter {
    private String fileName;
    FileOutputStream file;
    FileChannel fc ;
    protected final int bufferSize;
    protected final byte[] schema;
    private String mode = "rw";
    private int poolSize = Runtime.getRuntime().availableProcessors();
    
    /**
     * Index
     */
    private RandomAccessFile i;
	private MappedByteBuffer mbi;
	private FileChannel ifc;
	private AtomicLong index = new AtomicLong(0); //the counter of how many records we have written.
	
	
    /**
     * Possible pool of unsafe memory objects.
     */
    protected IPool<UnsafeMemory> pool;
    /**
     * pool of pre allocated byte buffers
     */
    protected IPool<ByteBuffer> bufferManager;
    
    private boolean allocateDirect = true; //setter

    public AbstractFileBinaryLogWriter(boolean useByteBuffer,byte[] schema, String name, String fileName) {
        super(name);
        this.fileName = fileName;
        this.useByteBuffer = useByteBuffer;
        this.schema = schema;
        bufferSize = ByteUtils.calcSchemaSize(schema,256);
        String strPoolSize = System.getProperty("binary.log.poolsize");
    	if (strPoolSize != null) {
    		poolSize = Integer.valueOf(strPoolSize);
    	}
		
        if (useByteBuffer) {
        	bufferManager = new SimplePool<ByteBuffer>(poolSize,
					new IObjectFactory<ByteBuffer>() {

						@Override
						public ByteBuffer make() {
							if (allocateDirect) {
	        	        		return ByteBuffer.allocateDirect(bufferSize);
	        	        	} else {
	        	        		return ByteBuffer.allocate(bufferSize);
	        	        	}
						}
					}); 
			
        } else {
        	pool = new SimplePool<UnsafeMemory>(poolSize,
					new IObjectFactory<UnsafeMemory>() {

						@Override
						public UnsafeMemory make() {
							byte[] buffer = new byte[bufferSize];
							return new UnsafeMemory(buffer);
						}
					}) {
			};
        }
    }

    public void start() {
        super.start();
        logger.info("Opening file " + fileName);

        try {
            //this.file = new RandomAccessFile(fileName,mode);
            this.file = new FileOutputStream(fileName);
            fc = file.getChannel();
            
            //index.
            this.i =new RandomAccessFile(fileName+".index",mode);
        	ifc = i.getChannel();
        	mbi = ifc.map(MapMode.READ_WRITE, 0, ByteUtils.calcSchemaSize(new byte[]{ByteUtils.Types.LONG}, 0));            
        	
            setCanLog(true);
            logger.info(fileName+ " is open");
            writeHeader();
        } catch (Exception e) {
            logger.error(e.getMessage());
            return;
        }


    }
    protected boolean useByteBuffer = false;
    public void setUseByteBuffer(boolean useByteBuffer) {
    	this.useByteBuffer = useByteBuffer;
    }

    
    /**
     * write num fields and byte for each field saying the type.
     * @throws IOException
     */
    private void writeHeader() throws IOException {
    	if (useByteBuffer) {
			ByteBuffer buf = ByteBuffer.allocate(4 + schema.length);
			buf.putInt(schema.length);
			for (byte o : schema) {
				buf.put(o);
			}
			writeBytes(buf,true);
			bufferManager.free(buf);
    	} else {
	    	UnsafeMemory um = pool.get();
	    	um.putInt(schema.length);
	    	for (byte o:schema) {
	          um.putByte(o);
	    	}
	    	writeBytes(um.getBytes());
	    	pool.free(um);
    	}
    }


    
    public void stop() {
        logger.info("Closing file " + fileName);
        setCanLog(false);
        try {
            fc.force(true);
            fc.close();
            file.close();
        } catch (Exception e) {
            logger.error(e.getMessage());
            return;
        }
        logger.info(fileName+ " is closed");

    }
    private long pos = 0;
    
    protected void writeBytes(byte[] bytes) {
    	if (canLog()) {
    		try {
    			file.write(bytes);
    		} catch (Exception e) {
                logger.error(e.getMessage());
            }	
    	}
    }
    
    protected void writeBytes(ByteBuffer buf, boolean isHeader) {
        if (canLog()) {
            try {
                buf.flip();

                if (isHeader) {
                	while(buf.hasRemaining()) {
	                    pos +=fc.write(buf);
	                }
                } else {
	                while(buf.hasRemaining()) {
	                    pos +=fc.write(buf);
	                }
	                updateIndex();
                }
            } catch (Exception e) {
                logger.error(e.getMessage());
            }
        }
    }

    private void updateIndex() {
		mbi.putLong(0,index.getAndIncrement());
	}
}
