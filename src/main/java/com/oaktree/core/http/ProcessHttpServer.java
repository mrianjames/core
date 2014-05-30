package com.oaktree.core.http;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.oaktree.core.container.AbstractComponent;
import com.oaktree.core.utils.Text;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

/**
 * A basic process that exposes http functionality.
 * 
 * @author ianjames
 * 
 */
@SuppressWarnings("restriction")
public class ProcessHttpServer extends AbstractComponent implements HttpHandler {
	private int port;
	private final static Logger logger = LoggerFactory.getLogger(ProcessHttpServer.class);

	private Map<String, Resource> resources = new HashMap<String, Resource>();
	private MBeanServer mbs;
	private final static String JAVASCRIPT = "text/javascript";
	private final static String LESS = "text/plain";
	private final static String HTML = "text/html";
	private final static String PLAIN = "text/plain";
	private final static String CSS = "text/css";
	private final static String FONTS = "text/plain";
	private final static String JPEG = "image/jpeg";
	private final static String GIF = "image/gif";
	private final static String PNG = "image/png";
	private final static String ICO = "image/x-icon";
	private final static String SVG = "image/svg+xml";
	private final static String WOFF = "application/x-font-woff";
	private final static String TTF = "application/x-font-ttf";// or //
	boolean isDebug = java.lang.management.ManagementFactory.getRuntimeMXBean().getInputArguments().toString().indexOf("jdwp") >= 0;												// "application/x-font-truetype"
	private String root;

	private final static String OTF = "application/x-font-opentype";
	private final static String EOT = "application/vnd.ms-fontobject";
	private final static String ENV = "env";
	private final static String TABLE_BINDING = "table_binding";
    private final static String DATA_VIEW = "data-view";
    private final static String DEFINE = "define";    
	private final static String SYS = "sys";
	private final static String PARAM = "param";
    private final static String VAR = "var";
    private final static String TVAR = "tvar";
    private final static String VALUE = "value";
    private final static String SERVICE = "service";
	private final static String JMX_MBEANS = "jmx";
    private final static String FOR="for";
	private final static String JAVA = "java";
	private final static String IMPORT = "import=";

    private String lineChartCodeTemplate;

    private String paginatedTableCodeTemplate;

    private static String dataViewLineChart = "line_chart";
    private static String dataViewPaginatedTable = "paginated_table";

    /**
     * ${data-view type="line_chart" options="" title="Heap" source="memory.getSnapshots('Heap')" source-fields="time(Time),used(Used)"}
     *
     * @param repl
     * @return
     */
    private String processDataViewLineChartRequest(String repl,ResourceContext ctx) throws Exception {
        //explode the options from the line...
        String[] options=getOption("options",repl).split("[,]");
        String source = replaceSourceParams(getOption("source", repl), ctx);
        String template = getOption("template", repl);
        String keyField = getOption("key-field", repl);
        String keyFormat = getOption("key-format", repl);
        String yAxisName = getOption("y-axis-name",repl,"Size(M)");
        String xAxisName = getOption("x-axis-name",repl,"Time");
        SimpleDateFormat keyFormatter = null;
        if (keyFormat != null) {
            keyFormatter = new SimpleDateFormat(keyFormat);
        }
        
        String title = checkAndReplaceVars(getOption("title", repl), ctx);
        String id = checkAndReplaceVars(getOption("id", repl, title+ "LineChart") , ctx).replace(" ","");
        String source_flds=getOption("source-fields", repl); //TODO refactor the use of this. TODO remove spaces etc.
        String sourceFields = "";
        int cols = 0;
        if (keyField != null) {
        	sourceFields += keyField+",";
        	cols++;
        }
        //List<String> columns = new ArrayList<String>();
        for (String field:source_flds.split(",")) {
            //int br = field.indexOf("("); //TODO should ban that syntax here.
            sourceFields += field+",";
            cols++;
            //columns.add(field.substring(br+1,field.length()-1));
        }
        String[] values = getValue(source+":"+sourceFields,ctx).split(","); //this should get us all our cols in a flat format. we dont want flat...
        if (values.length == 1 && values[0].equals("")) {
        	values = new String[]{};
        }
        
        //return makeLineChartCode(title,id,values,template,source_flds.split(","),keyFormatter,cols);
        String code = getAmChartData(values,sourceFields.split(","));
        return replaceChartTemplateWithValues(title,xAxisName,yAxisName,id,new String[]{code},template,sourceFields.split(","));
    }
    
    
    /**
     * Format is this.
     * [
						{
							"date": "07:57:57.567",
							"Used": "8.4",
							"Committed": 5
						},
						{
							"date": "07:57:58.567",
							"Used": 6,
							"Committed": 7
						},						
					]
     * @return
     */
    private String getAmChartData(String[] values, String[] fieldNames) {
    	StringBuilder b = new StringBuilder();
    	b.append("[\n");
    	int i = 0;
    	int rows = values.length/fieldNames.length;
    	int valuepos = 0;
    	for (int row = 0;row < rows;row++) {
    		i = 0;
    		b.append("\t{");
	    	for (String fieldName: fieldNames) {
	    		b.append(" \"");
	    		b.append(fieldName);
	    		b.append("\": \"");
	    		b.append(values[valuepos+i]);
	    		b.append("\"");
	    		if (i < fieldNames.length-1) {
	    			b.append(", ");
	    		}
	    		i++;
	    	}
	    	b.append("\t}");
	    	valuepos += fieldNames.length;
    		if (valuepos < values.length) {
    			b.append(",");
    		}
    		b.append("\n");
	    	
    	}
    	b.append("]");
    	return b.toString();
    }

    /**
     * For FLOT. Make data into right format.
     * @param title
     * @param chartId
     * @param values
     * @param template
     * @param dataSetNames
     * @param keyFormatter
     * @param cols
     * @return
     */
    public String generateFlotChart(String title,String chartId, String[] values, String template, String[] dataSetNames,DateFormat keyFormatter, int cols ) {
    	
    	int sets = cols;
        int dataSetSize = values.length/(sets);
        String[][] vs = new String[sets][dataSetSize];
        int pos = 0;
        //we need x datasets, the first is the "key", the rest are related to column 0 name, column 1 name etc.
        //30 items, 2 cols, 1 key. 10 rows of data per thingy.
        try {
            for (int i = 0; i < (values.length / cols); i++) { //go round 10 times.

                pos = (i * cols); //0,3,6,9...
                if (keyFormatter != null) {
                    vs[0][i] = String.valueOf(keyFormatter.parse(values[pos]).getTime() + Text.getToday());
                } else {
                    vs[0][i] = String.valueOf(values[pos]);
                }
                for (int j = 1; j < cols; j++) {
                    vs[j][i] = values[pos + j];
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Failure!!! pos:"+pos +" cols: "+cols + " valueslen: "+values.length,e);
        }

        
        List<String> jsDataSets = new ArrayList<String>();
        for (int i = 1; i < vs.length;i++) { // 3 datasets came in, only 2 will we parse.
            StringBuilder b = new StringBuilder("");

            for (int j = 0; j < vs[0].length; j++) {

                b.append("[");
                b.append(vs[0][j]);
                b.append(",");
                b.append(vs[i][j]);
                b.append("],");
            }
            String datastr = b.toString();
            if (datastr.length() > 0){
	            String data = datastr.substring(0, datastr.length() - 1);
	
	            jsDataSets.add(data);

            } else {
            	jsDataSets.add("[]");
            }
        }
        return replaceChartTemplateWithValues(title,"X","Y",chartId,jsDataSets.toArray(new String[jsDataSets.size()]),template,dataSetNames);        
    }

    
    private static String readTextFromResource(String file) {
    	StringBuilder b = new StringBuilder();
    	try {
	    	FileReader fr = new FileReader(file);
	    	BufferedReader br = new BufferedReader(fr);
			
			String line = br.readLine();
			while (line != null) {
				b.append(line);
				b.append("\n");
				line = br.readLine();
			}
			br.close();
    	} catch (Exception e) {
    		logger.error("Cannot load resource " + file + ": ",e);
    		return null;
    	}
		return b.toString();
    }

    private String replaceChartTemplateWithValues(String title,String xaxis, String yaxis,String chartId,String[] datasets,String template,String[] dataSetNames) {
    	if (template == null) {
        	template = "./web/oaktree/templates/LineChart.html";
        }
        String code = getLineChartTemplate(template).replaceAll("\\$\\{chart_id\\}",chartId);
        code = code.replaceAll("\\$\\{chart_title\\}",title);
        code = code.replaceAll("\\$\\{chart_y_axis_name\\}",yaxis);
        code = code.replaceAll("\\$\\{chart_x_axis_name\\}",xaxis);
        //${chart_data_key}
        code = code.replaceAll("\\$\\{chart_data_key\\}",dataSetNames[0]);
        int maxDataSets = 4;
        for (int set = 0; set < maxDataSets; set++) {
            String data = (set < datasets.length) ? datasets[set] : "";
            code = code.replaceAll("\\$\\{chart_data"+(set+1)+"\\}",data);
        }
        for (int set = 1; set < maxDataSets; set++) {
            String data = (set < dataSetNames.length) ? dataSetNames[set] : "";
            code = code.replaceAll("\\$\\{chart_dataset_name"+(set)+"\\}",data);
        }
    	    	
        return code;
    }


    //table_id, table_data, table_header.
    private String makePaginatedTableCode(String id, String title, String[] values, List<String> columns, String template) {
        String headerRows = "";
        for (String col:columns) {
            headerRows = headerRows+"<th>"+col+"</th>";
        }
        int columnCount = columns.size();

        String data = "";
        if (columnCount > 0) {
            //<tr><td>21:57:35.715</td><td>20803200</td><td>20185088</td><td>12627048</td><td>322371584</td></tr>
            int rows = values.length / columns.size();
            for (int i = 0; i < rows;i++) {
                data += "<tr>";
                int c = 0;
                for (String col : columns) {
                    data+="<td>";
                    data+=values[(i*columnCount)+c];
                    data+="</td>";
                    c++;
                }
                data += "</tr>";
            }
        }
        if (template == null) {
        	template = "./web/oaktree/templates/DataTable.html";
        }
        String code = getDataTableTemplate(template).replaceAll("\\$\\{table_headers\\}",headerRows);
        code = code.replaceAll("\\$\\{table_data\\}",data);
        code = code.replaceAll("\\$\\{table_id\\}",id);
        return code;
    }

	private String getDataTableTemplate(String templateName) {
		if (templateName != null && (paginatedTableCodeTemplate == null || isDebug)) {
			paginatedTableCodeTemplate = readTextFromResource(templateName);
		}
		return paginatedTableCodeTemplate;
	}
	private String getLineChartTemplate(String templateName) {
		if (templateName != null && (lineChartCodeTemplate == null || isDebug)) {
			lineChartCodeTemplate = readTextFromResource(templateName);
		}
		return lineChartCodeTemplate;
	}

	public ProcessHttpServer(int port) {
		this.port = port;
		mbs = ManagementFactory.getPlatformMBeanServer();
	}

	private static enum ResourceChunkType {
		VARIABLE,CODE,PLAIN,BINARY,IMPORT;
	}
	private static class ResourceChunk {
		public ResourceChunk(ResourceChunkType ct, String string) {
			this.chunkType = ct;
			this.string = string;
			this.bytes = string.getBytes();
		}
		public ResourceChunk(ResourceChunkType type, byte[] bites) {
			this.chunkType = type;
			this.bytes = bites;
		}
		private ResourceChunkType chunkType;
		public ResourceChunkType getChunkType() {
			return chunkType;
		}
		private String string;
		public String getString() {
			return string;
		}
		byte[] bytes = new byte[] {};

		public byte[] getBytes() {
			return bytes;
		}
		public boolean isBinary() {
			return chunkType.equals(ResourceChunkType.BINARY);
		}
		public boolean isVariable() {
			return chunkType.equals(ResourceChunkType.VARIABLE);
		}
		public String toString() {
			return chunkType.name() + " "+ (isBinary() ? (bytes.length + " bytes.") : isVariable() ? string : (string.length() + " chars"));
		}
	}

	private static class Resource {
		private String path;
		private File file;
		private String type;
		private boolean template;
		private List<ResourceChunk> chunks = new ArrayList<ResourceChunk>();
		public boolean isTemplate() {
			return template;
		}

		public String getPath() {
			return path;
		}


		public File getFile() {
			return file;
		}

		public String getType() {
			return type;
		}

		public Resource(String action, String type, File file) {
			this.path = action;
			this.file = file;
			this.type = type;
		}


		/**
		 * Go through the text file and work out any mappings we need to assess
		 * against services or variables we need to replace.
		 * @param string
		 */
		private void parseResource(String string) {
			String key_start = "${";
			String key_end = "}";
			int foundAt = 0;
			foundAt = string.indexOf(key_start, foundAt);
			if (foundAt == -1) {
				chunks.add(new ResourceChunk(ResourceChunkType.PLAIN,string));
				return;
			}
			int lastIndex = 0;
			while (foundAt >= 0) {
				//make a chunk
				if (foundAt-(lastIndex+1) > 0) {
					int start = lastIndex+1;
					if (lastIndex == 0) {
						//first chunk...
						start = 0;
					}
					ResourceChunk chunk = new ResourceChunk(ResourceChunkType.PLAIN,string.substring(start,foundAt));
					chunks.add(chunk);
				}
                //TODO nested ${}....
				//look for the end.
				int end = string.indexOf(key_end, foundAt);
				lastIndex= end;
				String text = string.substring(foundAt+key_start.length(),end);
				if (text.startsWith(IMPORT)) {
					String imports = text.split(IMPORT)[1];
					for (String imp:imports.split("[,]")) {
						ResourceChunk chunk = new ResourceChunk(ResourceChunkType.IMPORT,imp);
						chunks.add(chunk);
					}
				} else if (text.startsWith(FOR)) {
                    ResourceChunk chunk = new ResourceChunk(ResourceChunkType.CODE,text);
                    chunks.add(chunk);
                } else {
					ResourceChunk chunk = new ResourceChunk(ResourceChunkType.VARIABLE,text);
					chunks.add(chunk);
				}
				foundAt = string.indexOf(key_start, end+1);

			}
			chunks.add( new ResourceChunk(ResourceChunkType.PLAIN,string.substring(lastIndex+1,string.length()-1)));

			int i = 0;
			for (ResourceChunk chunk:chunks) {
				logger.info("Chunk "+i+": " + chunk.toString());
				i++;
			}
			//TODO other syntaxes?
		}

		public void load() {
			try {
				chunks.clear();
				FileReader fr = new FileReader(file);
				if (isText(type)) {
					logger.info("Resource is textual: " + type);
					String text = readTextFromResource(file.getAbsolutePath());
					parseResource(text); //TODO this needs to chunk it up

					logger.info("Loaded resource of " + chunks.size() + " chunks");

				} else {

					// binary and other files.
					logger.info("Resource is binary: " + type + " length: "
							+ file.length());
					Path path = Paths.get(file.getAbsolutePath());
					byte[] bites = Files.readAllBytes(path);
					ResourceChunk chunk = new ResourceChunk(ResourceChunkType.BINARY,bites);
					chunks.add(chunk);
					logger.info("Loaded resource of " + bites.length);
				}

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}




		private Map<String, String> services = new HashMap<String, String>();

		private void setServices(Map<String, String> services) {
			this.services.putAll(services);
		}

		private boolean isTemplate(String file) {
			// search for any ${...} strings and record them as template
			// variables we need to replace.
			List<String> tags = new ArrayList<String>();
			return false;
		}



		public boolean hasReplacements() {
			for (ResourceChunk chunk:chunks) {
				if (chunk.getChunkType().equals(ResourceChunkType.VARIABLE)) {
					return true;
				} else if (chunk.getChunkType().equals(ResourceChunkType.CODE)) {
					return true;
				} if (chunk.getChunkType().equals(ResourceChunkType.IMPORT)) {
					return true;
				}
				//check each imported

			}
			return false;
		}

		public List<ResourceChunk> getChunks() {
			return chunks;
		}


	}

	/**
	 * Find all the resources under this domain.
	 */
	public void discover() {
		File root = getRoot();
		logger.info("Loading resources from " + root);
		processOtherFolders(root);

		logger.info("Resources loaded: " + resources.size());
	}

	private File getRoot() {
		String web = System.getProperty("user.dir") + File.separator + "web";
		File dweb = new File(web);
		return dweb;
	}

    private class ResourceContext {
        public Map<String, String> params;
        public Map<String,String> variables;

        public void setVar(String var, String value) {
            if (variables == null) {
                variables=new HashMap<String,String>();
            }
            variables.put(var,value);
        }
        public String getValue(String variableName) {
            if (variables == null) {
                return null;
            }
            return variables.get(variableName);
        }
    }

	/**
	 * The key method. If this is a binary then its single chunk - return binary data.
	 * If its text it may have replacements - if it does not then return binary data.
	 * If replacements and text then we will have to go through the chunks, evaluate the values for
	 * the replacement parts and glue together into the final string, of which we return the binary data for.
	 * @param ctx - context of variables and parameters we have in scope (maybe from parent resource).
	 * @return
	 */
	public byte[] getData(Resource resource,ResourceContext ctx) throws Exception {
		if (!isText(resource.getType()) || !resource.hasReplacements()) {
			return resource.getChunks().get(0).getBytes();
		}
		StringBuilder builder = new StringBuilder(2048);
		for (ResourceChunk chunk:resource.getChunks()) {
			if (chunk.getChunkType().equals(ResourceChunkType.PLAIN)) {
				builder.append(chunk.getString());
			} else if (chunk.getChunkType().equals(ResourceChunkType.VARIABLE)) {
				String value = processVariableReplacement(chunk,ctx);
				builder.append(value);
			} else if (chunk.getChunkType().equals(ResourceChunkType.IMPORT)) {
				//lets go and get another resource and inject its content input here.
				Resource imp = resources.get(chunkTextToImport(chunk.getString()));
				if (imp != null) {

					builder.append(new String(getData(imp,ctx)));
				} else {
					logger.warn("Cannot find import "+chunkTextToImport(chunk.getString()));
				}

			} else if (chunk.getChunkType().equals(ResourceChunkType.CODE)) {
                //TODO am i a for loop or other code?????
                logger.info("handling code..."+chunk.getString());
                //get the code from the template name...
                String template = getOption("template",chunk.getString());
                String source = getOption("source",chunk.getString());
                String var = getOption("var",chunk.getString());
                String strValues= getValue(source, ctx);
                String[] values = strValues.split(",");
                Resource rt = resources.get(template);
                if (rt != null) {
                	if (isDebug) {
                		rt.load(); //reload it on debug pls.
                	}
                    for (String value:values) {
                        ctx.setVar(var,value);
                        builder.append(new String(getData(rt, ctx)));
                    }
                }  else {
                    logger.warn("Cannot find iteration template "+chunkTextToImport(chunk.getString()));
                }
            }
            else {
				//TODO - code! THIS MEANS FORS
			}
		}
		return builder.toString().getBytes();
	}

	private String processVariableReplacement(ResourceChunk chunk,ResourceContext ctx) throws Exception {
		String repl = chunk.getString();
			String value = getValue(repl,ctx);
			return value;
	}

	private String getValue(String repl, ResourceContext ctx) throws Exception {
		String value= "";
		if (repl.startsWith(ENV)) {
			value = processEnvironmentVariable(repl,ctx);
		} else if (repl.startsWith(JAVA)) {
            value = processJavaStaticMethod(repl,ctx);
		}else if (repl.startsWith(SYS)) {
			value = System.getProperty(chunkTextToVariableName(repl));
		} else if (repl.startsWith(DATA_VIEW)) {
            value = this.processDataViewRequest(repl,ctx);
        } else if (repl.startsWith(DEFINE)) {
            value = this.processDefineVariable(repl,ctx);
        }else if (repl.startsWith(TABLE_BINDING)) {
			throw new IllegalStateException("Deprecated api. pls upgrade to "+DATA_VIEW);
		} else if (repl.startsWith(PARAM)) {
			value = ctx.params.get(chunkTextToVariableName(repl));
		} else if (repl.startsWith(JMX_MBEANS)) {
			value = processJmx(repl,ctx);
		} else if (repl.startsWith(VAR)) {
            value = ctx.getValue(chunkTextToVariableName(repl));
        } else if (repl.startsWith(TVAR)) {
            value = ctx.getValue(chunkTextToVariableName(repl));
            if (value != null) {
                value = value.replaceAll(" ","");
            }
        } else if (repl.startsWith(VALUE)) {
            value = repl.substring(VALUE.length());
        } else if (repl.startsWith(SERVICE)) {
            value = processService(repl,ctx);
		} else {
            //mmmm. dodgy.
            //value = processService(repl,ctx);
            return repl;
            //throw new IllegalStateException("Invalid value type requested..."+repl);
        }
		return value;
	}

    private String processDefineVariable(String repl, ResourceContext ctx) throws Exception {
		String expr = repl.substring(repl.indexOf(".",6)+1);
		String[] bits = expr.split("=");
		String var = bits[0];
		String value = bits[1];
		value = getValue(value,ctx);
		ctx.setVar(var, value);
		return "";
	}

	private String processEnvironmentVariable(String repl, ResourceContext ctx) throws Exception {
        String value = "";
        String e = chunkTextToVariableName(repl);
        if (e.startsWith("all")) {
            value = flattenBulkReplacement(System.getenv());
        } else {
            value = System.getenv(e);
        }
        return value;
    }

    private String processJavaStaticMethod(String repl, ResourceContext ctx) throws Exception {
        String value = "";
        //${java.java.lang.System.currentTimeMillis()}
        String clazz = chunkTextToJavaClassName(repl);
        String method = chunkTextToJavaMethodName(repl);
        //TODO clarifications.
        String[] args = chunkTextToJavaArgs(repl,ctx);
        
        Object v = executeMethodWithArgs(null,Class.forName(clazz), method, args);
        value = String.valueOf(v);
        //TODO arrays...
        return value;
    }

    private String[] chunkTextToJavaArgs(String repl,ResourceContext ctx) throws Exception {
    	repl = repl.substring(repl.lastIndexOf('(')+1,repl.lastIndexOf(')'));
    	if (repl.length() < 1) {
    		return new String[]{};
    	}
		String[] bits = repl.split("[,]");
		String[] values = new String[bits.length];
		int i = 0;
		for (String bit:bits) {
			String v = getValue(bit,ctx);
			values[i] = v;
			i++;
		}
		return values;
	}

	private String processJmx(String repl, ResourceContext ctx) throws Exception {
        String value = "";
        ObjectName mObjectName = new ObjectName( chunkTextToJmxObjectName(repl) );
        //get ze beanie.
        Object ovalue = this.mbs.getAttribute(mObjectName, chunkTextToJmxAttributeName(repl));
        try {
            value = String.valueOf(ovalue);
        } catch (Exception e) {
            try {
                value = (String)ovalue;
            } catch (Exception f) {
                try {
                    value = ovalue.toString();
                } catch (Exception g) {
                    logger.error("Cant convert object value to string: " + ovalue.getClass().getName(),e);
                }
            }
        }
        return value;
    }

    private String processService(String phrase, ResourceContext ctx) throws Exception {
        String repl = phrase;

        if (phrase.startsWith(SERVICE)) {
            repl = phrase.substring(SERVICE.length()+1);
        }
        //might be a variable.
        String value = ctx.getValue(repl);
        if (value == null) {
            value = "";
            //loaded service..? TODO make this generic e.g. by service name. so service.latency.getLatency("xyz")
            //${latency_service.getLatency("xyz")}

            int dot = repl.indexOf('.');
            int bkt = repl.indexOf('(');
            int pend = repl.indexOf(')');
            int clarstart = repl.indexOf(':');
            String service = repl.substring(0, dot);
            String method = repl.substring(dot + 1, bkt);
            String clarification = null;
            if (clarstart > -1) {
                clarification = repl.substring(clarstart + 1, repl.length());
            }
            //method parameters.
            String[] pms = new String[]{};
            String p = repl.substring(bkt + 1, pend);
            if (p != null && p.length() > 0) {
                pms = p.split(",");
            }
            for (int i = 0; i <pms.length;i++) {
                if (pms[i].contains(":")) {
                    pms[i] = getValue(pms[i],ctx);
                }
            }
            Object o = this.services.get(service);
            if (o != null) {
                Object v = executeMethodWithArgs(o,o.getClass(),method,pms);
                if (clarification == null) {
                    
                    //handle non castable things....like array..
                    if (v.getClass().isArray()) {
                        int sz = Array.getLength(v);
                        for (int i = 0; i < sz; i++) {
                            if (value == null) {
                                value = "";
                            }
                            Object ch = Array.get(v, i);
                            value += String.valueOf(ch) + ",";
                        }
                    } else {
                        value = String.valueOf(v);
                        //TODO any others?
                    }
                } else {
                    

                    //TOOD complex obj; get the values into proper structure.
                    if (v.getClass().isArray()) {
                        Class ofArray = v.getClass().getComponentType();
                        int sz = Array.getLength(v);
                        for (int i = 0; i < sz; i++) {
                            Object ch = Array.get(v, i);
                            for (String c : clarification.split("[,]")) {
                                String cname = "get" + Character.toUpperCase(c.charAt(0)) + c.substring(1);
                                Method x = ofArray.getMethod(cname);
                                if (x.getReturnType().equals(double.class) || x.getReturnType().equals(float.class)) {
                                	value += Text.to4Dp((double)x.invoke(ch))+",";
                                } else {
                                	value += String.valueOf(x.invoke(ch)) + ",";
                                }
                            }
                        }
                       //TODO collections.
                    } else {
                        for (String c : clarification.split("[,]")) {
                            String cname = "get" + Character.toUpperCase(c.charAt(0)) + c.substring(1);
                            Method x = v.getClass().getMethod(cname);
                            value += String.valueOf(x.invoke(v)) + ",";
                        }
                    }
                    if (value.length() > 0) {
                        value = value.substring(0, value.length() - 1);
                    }
                }
            }
        }
        return value;
    }

    /**
     * On an object, execute a method with args we have specified - we will cast as we can.
     * THIS WILL NOT WORK IF YOU HAVE A METHOD NAME WITH SAME NUM ARGS OF DIFFERENT TYPES.
     * @param o
     * @param method
     * @param pms
     * @return
     * @throws Exception
     */
    private Object executeMethodWithArgs(Object o,Class clazz,String method, String[] pms) throws Exception {
    	Method m = null;
        Object[] vars = new Object[pms.length];
        for (Method mtd : clazz.getMethods()) {
            if (mtd.getName().equals(method)) {
                Class<?>[] ptypes = mtd.getParameterTypes();
                if (ptypes.length == pms.length && canCastAllArgs(pms,ptypes)) {
                    //TODO better checking.
                    int i = 0;
                    for (Class<?> type : ptypes) {
                    	try {
                    		vars[i] = type.cast(pms[i]);
                    	} catch (Exception e) {
                    		//try some common ops...
                    		if (type.equals(Long.class) || type.equals(long.class)) {
                    			vars[i] = Long.valueOf(pms[i]);
                    		} else if (type.equals(Double.class)|| type.equals(double.class)) {
                    			vars[i] = Double.valueOf(pms[i]);
                    		} else if (type.equals(Integer.class)|| type.equals(int.class)) {
                    			vars[i] = Integer.valueOf(pms[i]);
                    		} else if (type.equals(Short.class)|| type.equals(short.class)) {
                    			vars[i] = Short.valueOf(pms[i]);
                    		} else if (type.equals(Float.class)|| type.equals(float.class)) {
                    			vars[i] = Float.valueOf(pms[i]);
                    		} else if (type.equals(Boolean.class)|| type.equals(boolean.class)) {
                    			vars[i] = Boolean.valueOf(pms[i]);
                    		}
                    	}
                    	
                        i++;
                    }
                    m = mtd;
                    break;
                }
            }
        }
        if (o != null) {
        	return m.invoke(o, vars);
        } else {
        	return m.invoke(null, vars);
        }
	}

	private boolean canCastAllArgs(String[] pms, Class<?>[] ptypes) {
		//TODO better checking.
        int i = 0;
        for (Class<?> type : ptypes) {
        	Object var = null;
        	try {
        		var = type.cast(pms[i]);
        	} catch (Exception e) {
        		//try some common ops...
        		if (type.equals(Long.class) || type.equals(long.class)) {
        			var = Long.valueOf(pms[i]);
        		} else if (type.equals(Double.class)|| type.equals(double.class)) {
        			var = Double.valueOf(pms[i]);
        		} else if (type.equals(Integer.class)|| type.equals(int.class)) {
        			var = Integer.valueOf(pms[i]);
        		} else if (type.equals(Short.class)|| type.equals(short.class)) {
        			var = Short.valueOf(pms[i]);
        		} else if (type.equals(Float.class)|| type.equals(float.class)) {
        			var = Float.valueOf(pms[i]);
        		} else if (type.equals(Boolean.class)|| type.equals(boolean.class)) {
        			var = Boolean.valueOf(pms[i]);
        		} else {
        			return false;
        		}
        	}
        	
            i++;
        }
        return true;
	}

	/**
     * Process the following format:
     * ${data-view type="line_chart" options="" title="Heap" source="memory.getSnapshots('Heap')" source-fields="time(Time),used(Used)"}
     *
     * various types will be supported and parsed differently.
     * TODO refactor this stuff to be plugin/template based.
     *
     * @param repl
     * @return
     */
    private String processDataViewRequest(String repl,ResourceContext ctx) throws Exception {
        String type = getDataViewType(repl);
        if (type.equals(dataViewLineChart)) {
            return processDataViewLineChartRequest(repl,ctx);
        } else if (type.equals(dataViewPaginatedTable)) {
            return processDataViewPaginatedTableRequest(repl,ctx);
        }
        throw new IllegalArgumentException("Invalid template type.");
    }

    //${data-view type="paginated_table" options="" title="Heap" source="memory.getSnapshots(Heap)" source-fields="time(Time),init(Init),committed(Committed),used(Used),max(Max)"}
    private String processDataViewPaginatedTableRequest(String repl,ResourceContext ctx) throws Exception {
        //explode the options from the line...
        String[] options=getOption("options",repl).split("[,]");
        String source = replaceSourceParams(getOption("source", repl), ctx);
        String title = checkAndReplaceVars(getOption("title",repl),ctx);
        String template = getOption("template",repl);
        String columns = getOption("columns",repl);
        String id = checkAndReplaceVars(getOption("id",repl,title+"PagTable"),ctx).replace(" ","");
        String source_flds=getOption("source-fields",repl); //TODO refactor the use of this. TODO remove spaces etc.
        String sourceFields = "";
        List<String> dcolumns = new ArrayList<String>();
        if (columns != null) {
	        for (String col:columns.split(",")) {
	        	dcolumns.add(col);
	        }
        }
        if (source_flds != null) {
	        for (String field:source_flds.split(",")) {
	            int br = field.indexOf("(");
	            sourceFields += field.substring(0,br)+",";
	            dcolumns.add(field.substring(br+1,field.length()-1));
	        }
        }
        String[] values = getValue(source+":"+sourceFields,ctx).split(",");
        //TODO fields.
        String code = makePaginatedTableCode(id, title, values, dcolumns,template);
        return code;
    }



 

    private String replaceSourceParams(String source, ResourceContext ctx) throws Exception {
        int s = source.indexOf("(");
        int e = source.indexOf(")");
        if (s < 0 || e < 0) { //no params. maybe something like env.
        	return source;
        }
        String args = source.substring(s+1,e);
        String x = "";
        for (String arg:args.split("[,]")) {
            x = x+getValue(arg,ctx)+",";
        }
        if (x.length() > 0) {
            x = x.substring(0,x.length()-1);
        }
        return source.substring(0,s+1) + x+source.substring(e);
    }

    private String checkAndReplaceVars(String repl, ResourceContext ctx) {
        if (repl.contains(".")) {
            try {
                return getValue(repl, ctx);
            } catch (Exception e) {
                logger.error("Cannot parse replacement "+repl,e);
                return null;
            }
        }
        return repl;
    }

    private String getOption(String option, String phrase) {
        return getOption(option,phrase,null);
    }
    private String getOption(String option, String phrase, String defaultValue) {
        int s = phrase.indexOf(option+"=");
        if (s == -1) {
            return defaultValue;
        }
        int e = s+option.length()+2;
        int end = phrase.indexOf('"',e);
        return phrase.substring(e,end);

    }

    private String getDataViewType(String repl) {
        return getOption("type",repl);
    }

    //${binding=(Env Variables)[Var,Value][env.all]}
	private String chunkTextToBindingTitle(String repl) {
		return repl.substring(repl.indexOf('(')+1, repl.indexOf(')'));
	}

	private String chunkTextToJavaMethodName(String repl) {
		repl = repl.substring(0,repl.indexOf("("));
		String str = repl.substring(repl.lastIndexOf('.')+1);
		return str;
	}

	//${java.java.lang.System.currentTimeMillis()}
	private String chunkTextToJavaClassName(String repl) {
		repl = repl.substring(0,repl.indexOf("("));
		String str = repl.substring(repl.indexOf('.')+1, repl.lastIndexOf('.'));
		return str;
	}
	//TODO or use component mgr?
	private Map<String,Object> services = new HashMap<String,Object>();
	public void addService(String name,Object service) {
		services.put(name,service);
	}
	public void setServices(Map<String,Object> services) {
		this.services.putAll(services);
	}

	private String flattenBulkReplacement(Map<String, String> map) {
		StringBuilder b = new StringBuilder();
		for (Entry<String,String> e:map.entrySet()) {
			if (e.getKey().contains(",")) {
				logger.info("Key with comma");
			}
			if (e.getValue().contains(",")) {
				logger.info("Key with comma");
			}
			if (e.getValue().contains("=")) {
				logger.info("Key with comma");
			}
			if (!e.getKey().contains("=")) {
				b.append(e.getKey());
				b.append(",");
				String v = e.getValue().replace("\\", "\\\\");
				v = v.replace(",", "");
				b.append(v);
				b.append(",");
			}
		}
		String str = b.toString();
		return str.substring(0,str.length()-1);
	}

	//${binding=[Var,Value][env.all]}
	private String chunkTextToBindingData(String repl) {
		int colend = repl.indexOf(']');
		int s = repl.indexOf('[',colend);
		int e = repl.indexOf(']',s);
		return repl.substring(s+1, e);
	}

	//${binding=[Var,Value][env.all]}
	private String[] chunkTextToBindingColumns(String repl) {
		return repl.substring(repl.indexOf('[')+1,repl.indexOf(']')).split(",");
	}

	public String chunkTextToVariableName(String name) {
		return name.substring(name.indexOf('.')+1);
	}
	public String chunkTextToJmxObjectName(String name) {
		String variable = chunkTextToVariableName(name);
		return variable.substring(0,variable.lastIndexOf('.'));
	}
	public String chunkTextToJmxAttributeName(String name) {
		String variable = chunkTextToVariableName(name);
		return variable.substring(variable.lastIndexOf('.')+1);
	}
	public String chunkTextToImport(String name) {
		return name;
	}

	/**
	 * process all folders and files using extensions of files to drive their
	 * mime types.
	 *
	 * @param folder
	 */
	private void processOtherFolders(File folder) {
		for (File file : folder.listFiles()) {

			if (file.isDirectory()) {
				processOtherFolders(file);
			} else {
				String name = file.getName();
				String suffix = getSuffix(name);
				String type = getTypeForSuffix(suffix);
				loadResource(file, type); // todo
			}

		}
	}

	private String getSuffix(String name) {
		return name.substring(name.lastIndexOf('.') + 1,
				name.length());
	}

	private String getTypeForSuffix(String suffix) {
		String type = PLAIN;
		if (suffix.equals("ico")) {
			type = ICO;
		} else if (suffix.equals("png")) {
			type = PNG;
		} else if (suffix.equals("gif")) {
			type = GIF;
		} else if (suffix.equals("js")) {
			type = JAVASCRIPT;
		} else if (suffix.equals("html")) {
			type = HTML;
		} else if (suffix.equals("css")) {
			type = CSS;
		} else if (suffix.equals("less")) {
			type = LESS;
		} else if (suffix.equals("eot")) {
			type = EOT;
		} else if (suffix.equals("svg")) {
			type = SVG;
		} else if (suffix.equals("ttf")) {
			type = TTF;
		} else if (suffix.equals("woff")) {
			type = WOFF;
		} else if (suffix.equals("otf")) {
			type = OTF;
		} else if (suffix.equals("map")) {
			type = PLAIN;
		} else {
			logger.warn("Invalid suffix type: " + suffix
					+ " - defaulting to " + PLAIN);
			type = PLAIN;
			// throw new IllegalStateException("No matching type");
		}
		return type;
	}

	public static boolean isText(String type) {
		if (type.equals(LESS) || type.equals(JAVASCRIPT) || type.equals(HTML)
				|| type.equals(PLAIN) || type.equals(CSS)) {
			return true;
		}
		return false;
	}

	private void loadTemplate(String template) {
		File f = new File(template);
		
	}
	/**
	 * Load and cache a resource.
	 *
	 * @param file
	 * @param type
	 */
	private void loadResource(File file, String type) {
		String web = System.getProperty("user.dir") + File.separator + "web";
		int len = web.length();
		String path = null;
		try {
			path = file.getCanonicalPath().substring(len + 1,
					file.getCanonicalPath().length());
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		logger.info("Loading resource " + file.getName() + " with path of "
				+ path);
		Resource resource = new Resource(path, type, file);
		resources.put(path, resource);
		resource.load();
	}

	private static Map<String,String> getParameters(String query) {
		Map<String,String> result = new HashMap<String,String>();
		if (query == null) {
			return result;
		}
		for (String param:query.split("&")) {
			String pair[] = param.split("=");
			if (pair.length > 1) {
				result.put(pair[0], pair[1]);
			} else {
				result.put(pair[0], "");
			}
		}
		return result;
	}

	@Override
	public void handle(HttpExchange t) throws IOException {
		try {
			InputStream is = t.getRequestBody();
			while (is.read() != -1) {
				is.skip(0x100000);
			}

			OutputStream body = t.getResponseBody();
			long time = System.currentTimeMillis();
			Map<String, String> params = getParameters(t.getRequestURI().getQuery());

			String address = t.getRequestURI().toString();
			int endoff = address.length();
			if (address.contains("?")) {
				endoff = address.indexOf('?');
			}
			String path = address.substring(1, endoff);
			if (path == null || path.length() == 0) {
				path = "index.html";
			}
			if (File.separator.equals("\\") && path.contains("/")) {
				//windows but web url requested. convert url.
				path = path.replace("/",File.separator);
			}
			Resource resource = resources.get(path);
			if (resource == null || resource.getChunks() == null  || resource.getChunks().size() == 0) {
				//try again...
				File f = new File(getRoot()+File.separator+path);
				if (f.exists()) {
					loadResource(f,getTypeForSuffix(getSuffix(f.getName())));
					resource = resources.get(path);
				} else {
					byte[] msg = ("Resource not found: " + path).getBytes();
					t.sendResponseHeaders(200, msg.length);
					body.write(msg);
					body.close();
					return;
				}
			}
            if (resource.getChunks() == null || resource.getChunks().size() == 0) {
                byte[] msg = ("Not a valid endpoint (maybe directory?): " + path).getBytes();
                t.sendResponseHeaders(200, msg.length);
                body.write(msg);
                body.close();
                return;
            }
			Headers h = t.getResponseHeaders();
			h.set("Content-Type", resource.getType());
			h.set("Server", "OaktreeHttpServer/1.0 (Simple 4.0)");
			h.set("Date", String.valueOf(time));
			h.set("Last-Modified", String.valueOf(time));
			//String content = request.getContent();
			
			if (isDebug && isText(resource.getType())) {
				logger.info("Reloading resource as debug is enabled");
				loadResource(resource);
				
			}
			ResourceContext ctx = new ResourceContext();
            ctx.params = params;
			byte[] data = getData(resource,ctx);
			t.sendResponseHeaders(200, data.length);
			body.write(data);
			
			body.flush();
			body.close();
			logger.info("Processed");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	

	private void loadResource(Resource resource) {
		resource.load();
		for (ResourceChunk chunk:resource.getChunks()) {
			if (chunk.getChunkType().equals(ResourceChunkType.IMPORT)) {
				Resource r = resources.get(chunkTextToImport(chunk.getString()));
				if (r!=null) {
					loadResource(r);
				}
			}
		}
	}

	@Override
	public void start() {
		try {
			ThreadFactory tf = new ThreadFactory() {
				private AtomicInteger id = new AtomicInteger(0);
				@Override
				public Thread newThread(Runnable r) {
					Thread pt = new Thread(r);
					pt.setName("HttpThread"+id.getAndIncrement());
					pt.setPriority(Thread.MIN_PRIORITY); //so we dont impact much.
					return pt;
				}};
				ThreadPoolExecutor exec = new ThreadPoolExecutor(1, 3, 0, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(),tf);
			exec.prestartAllCoreThreads();
			HttpServer server = HttpServer.create(new InetSocketAddress(port),0);
			
			server.createContext("/",this);
			server.setExecutor(exec);
			server.start();

			// load stuff
			discover();
			logger.info("Http server is started on "+port + " mode: " +(isDebug? "DEBUG":"RUNTIME"));
		} catch (Exception e) {

		}
	}

	

}
