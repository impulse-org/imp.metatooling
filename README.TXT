
Steps

 1. Launch a runtime workbench
 2. Create a new plugin project
 3. Use the 'New' wizard to create a new programming language
 4. Edit the plugin.xml file
 5. Add these two builders (until we have an LPG wizard for this):
  
---------------------------------------------------------
		<buildCommand>
			<name>org.jikes.lpg.tooling.LpgBuilder</name>
			<arguments>
			</arguments>
		</buildCommand>
		<buildCommand>
			<name>org.jikes.lpg.tooling.XmlBuilder</name>
			<arguments>
			</arguments>
		</buildCommand>
---------------------------------------------------------

   6. Edit the .g file and save it. This activates the lpg builder, which will
      generate an xml file in your bin directory (use the Navigator View to see it).
   7. The creation of the XML files activates the Xml Builder that generates some
   	  broken Java files. More work needed here.
   	  
   	  

 
