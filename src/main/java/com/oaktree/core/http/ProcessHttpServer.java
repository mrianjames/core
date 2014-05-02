package com.oaktree.core.http;

import com.oaktree.core.container.AbstractComponent;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.io.*;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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
	//private String tableHeader1 = "<div class=\"row\">\n<div class=\"col-lg-12\">\n<h1 class=\"page-header\">";
	//Environment Variables
	private String tableHeader2="<div class=\"row\">\n<div class=\"col-lg-12\">\n<div class=\"panel panel-default\">\n<div class=\"panel-heading\">";
	//"System Variables"
    private String tableHeader3="</div><div class=\"panel-body\"><div class=\"table-responsive\"><table class=\"table table-bordered table-hover table-striped\" id=\"";
    private String tableHeader4="\">";
    private String tableTail = "</table></div></div></div></div></div>";
	private final static String OTF = "application/x-font-opentype";
	private final static String EOT = "application/vnd.ms-fontobject";
	private final static String ENV = "env";
	private final static String TABLE_BINDING = "table_binding";
	private final static String SYS = "sys";
	private final static String PARAM = "param";
	private final static String JMX_MBEANS = "jmx";
	private final static String JAVA = "java";
	private final static String IMPORT = "import=";
	
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
					BufferedReader br = new BufferedReader(fr);
					StringBuilder b = new StringBuilder();
					String line = br.readLine();
					while (line != null) {
						b.append(line);
						b.append("\n");
						line = br.readLine();
					}
					br.close();
					parseResource(b.toString()); //TODO this needs to chunk it up
					
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

	/**
	 * The key method. If this is a binary then its single chunk - return binary data.
	 * If its text it may have replacements - if it does not then return binary data.
	 * If replacements and text then we will have to go through the chunks, evaluate the values for
	 * the replacement parts and glue together into the final string, of which we return the binary data for.
	 * @param values
	 * @return
	 */
	public byte[] getData(Resource resource,Map<String, String> params) throws Exception {
		if (!isText(resource.getType()) || !resource.hasReplacements()) {
			return resource.getChunks().get(0).getBytes();
		}
		StringBuilder builder = new StringBuilder(2048);
		for (ResourceChunk chunk:resource.getChunks()) {
			if (chunk.getChunkType().equals(ResourceChunkType.PLAIN)) {
				builder.append(chunk.getString());
			} else if (chunk.getChunkType().equals(ResourceChunkType.VARIABLE)) {
				String value = processVariableReplacement(chunk,params);
				builder.append(value);
			} else if (chunk.getChunkType().equals(ResourceChunkType.IMPORT)) {
				//lets go and get another resource and inject its content input here.
				Resource imp = resources.get(chunkTextToImport(chunk.getString()));
				if (imp != null) {
					
					builder.append(new String(getData(imp,params)));
				} else {
					logger.warn("Cannot find import "+chunkTextToImport(chunk.getString()));
				}

			} else {
				//TODO - code!
			}
		}
		return builder.toString().getBytes();
	}
	
	private String processVariableReplacement(ResourceChunk chunk,Map<String,String> params) throws Exception {
		String repl = chunk.getString();
			String value = getValue(repl,params);
			return value;
	}

	private String getValue(String repl, Map<String, String> params) throws Exception {
		String value= "";
		if (repl.startsWith(ENV)) {
			String e = chunkTextToVariableName(repl);
			if (e.equals("all")) {
				value = flattenBulkReplacement(System.getenv());
			} else {
				value = System.getenv(e);
			}
		} else if (repl.startsWith(JAVA)) {
			//${java.java.lang.System.currentTimeMillis()}
			String clazz = chunkTextToJavaClassName(repl);
			String method = chunkTextToJavaMethodName(repl);
			Class c = Class.forName(clazz);
			Method m = c.getMethod(method);
			if (m != null) {
				value = String.valueOf(m.invoke(null));
			}
			
		}else if (repl.startsWith(SYS)) {
			value = System.getProperty(chunkTextToVariableName(repl));
		} else if (repl.startsWith(TABLE_BINDING)) {
			//${binding=(Env Variables)[Var,Value][env.all]}
			String[] columns = chunkTextToBindingColumns(repl);
			String binding = chunkTextToBindingData(repl);
			String title1 = chunkTextToBindingTitle(repl);
			//value+=tableHeader1 ;
			//value+=title1;
			value+=tableHeader2;
			value+=title1;
			value+=tableHeader3;
			value+="table_"+title1;
			value+=tableHeader4;
			value+="<thead>\n<tr>\n";                                   
			for (String col:columns) {
				value += "<th>"+col+"</th>\n";
			}
			value+="</tr>\n</thead>\n<tbody>\n";
			String bindingValue = getValue(binding,params);
			String[] bits = bindingValue.split("[,]");
			int i = 0;
			for (String bit:bits) {
				if (i == 0) {
					value+="<tr>";
				}
				value+="<td>";
				value+=bit;
				value+="</td>";
				if ((i+1)%columns.length == 0 && i>0) {
					if (i < bits.length-1) {
						value+="</tr>\n<tr>";
					} else {
						value+="</tr>\n";
					}
				}
				i++;
			}
			value+="</tbody>";
			value+=tableTail;
			//now values.
		} else if (repl.startsWith(PARAM)) {
			value = params.get(chunkTextToVariableName(repl));
		} else if (repl.startsWith(JMX_MBEANS)) {
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
		} else {
			//loaded service..?
			//${latency_service.getLatency("xyz")}
			int dot = repl.indexOf('.');
			int bkt = repl.indexOf('(');
			int pend = repl.indexOf(')');
			int clarstart = repl.indexOf(':');
			String service = repl.substring(0,dot);
			String method = repl.substring(dot+1,bkt);
			String clarification = null;
			if (clarstart > -1) {
				clarification = repl.substring(clarstart+1,repl.length());
			}
			String[] pms = new String[]{};
			String p = repl.substring(bkt+1,pend);
			if (p != null && p.length() > 0) {
				pms = p.split(",");
			}
			Object o = this.services.get(service);
			if (o != null)  {
				Method m = null;
				Object[] vars = new Object[pms.length];
				for (Method mtd:o.getClass().getMethods()) {
					if (mtd.getName().equals(method)) {
						Class<?>[] ptypes = mtd.getParameterTypes();
						if (ptypes.length == pms.length) {
							//TODO better checking.
							int i = 0;
							for (Class<?> type:ptypes) {
								vars[i] = type.cast(pms[i]);
								i++;
							}
							m = mtd;
							break;
						}
					}
				}
				if (clarification == null)  {
					value = String.valueOf(m.invoke(o, vars));
				} else {
					Object obj = m.invoke(o, vars);
					
					//TOOD complex obj; get the values into proper structure.
					if (obj.getClass().isArray()) {
						Class ofArray = obj.getClass().getComponentType();
						int sz = Array.getLength(obj);
						for (int i = 0; i < sz; i++ ){
							Object ch = Array.get(obj, i);
							for (String c:clarification.split("[,]")) {
								String cname = "get"+Character.toUpperCase(c.charAt(0))+c.substring(1);
								Method x = ofArray.getMethod(cname);
								value+=String.valueOf(x.invoke(ch))+",";
							}
						}
					} else {
						for (String c:clarification.split("[,]")) {
							String cname = "get"+Character.toUpperCase(c.charAt(0))+c.substring(1);
							Method x = obj.getClass().getMethod(cname);
							value+=String.valueOf(x.invoke(obj))+",";
						}
					}
					if (value.length() > 0){
						value = value.substring(0,value.length()-1);
					}
				}
			}
		}
		return value;
	}
	//${binding=(Env Variables)[Var,Value][env.all]}
	private String chunkTextToBindingTitle(String repl) {
		return repl.substring(repl.indexOf('(')+1, repl.indexOf(')'));
	}

	private String chunkTextToJavaMethodName(String repl) {
		String str = repl.substring(repl.lastIndexOf('.')+1,repl.lastIndexOf('('));
		return str;
	}

	//${java.java.lang.System.currentTimeMillis()}
	private String chunkTextToJavaClassName(String repl) {
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
			b.append(e.getKey());
			b.append(",");
			b.append(e.getValue());
			b.append(",");
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
			Resource resource = resources.get(path);
			if (resource == null) {
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
			
			byte[] data = getData(resource,params);
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

			HttpServer server = HttpServer.create(new InetSocketAddress(port),0);
			server.createContext("/",this);
			server.setExecutor(null);
			server.start();

			// load stuff
			discover();
			logger.info("Http server is started on "+port + " mode: " +(isDebug? "DEBUG":"RUNTIME"));
		} catch (Exception e) {

		}
	}

	

}
