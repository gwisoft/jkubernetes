#!/bin/env python

import os
import sys
import random
import subprocess as sub
import getopt
import time

def identity(x):
    return x

def cygpath(x):
    command = ["cygpath", "-wp", x]
    p = sub.Popen(command,stdout=sub.PIPE)
    output, errors = p.communicate()
    lines = output.split("\n")
    return lines[0]

if sys.platform == "cygwin":
    normclasspath = cygpath
else:
    normclasspath = identity

CUSTOM_CONF_FILE = ""
CONFIG_OPTS = []
STATUS = 0
JKUBERNETES_DIR = "/".join(os.path.realpath( __file__ ).split("/")[:-2])
JKUBERNETES_CONF_DIR = os.getenv("JKUBERNETES_CONF_DIR", JKUBERNETES_DIR + "/conf" )
CONFIG_OPTS = []
EXCLUDE_JARS = []
INCLUDE_JARS = []

API_SERVER_ADDRESS = ""
JKUBERNETES_CREATE_YAML_PATH = ""

def check_java():
    check_java_cmd = 'which java'
    ret = os.system(check_java_cmd)
    if ret != 0:
        print("Failed to find java, please add java to PATH")
        sys.exit(-1)

def print_commands():
    """Print all client commands and link to documentation"""
    print ("kubectl command [-s http://apiserverip:port] [--config client_jkubernetes.yaml] [--exclude-jars exclude1.jar,exclude2.jar] [-c key1=value1,key2=value2][command parameter]")
    print ("Commands:\n\t",  "\n\t".join(sorted(COMMANDS.keys())))
    print ("\n\t[--config client_jkubernetes.yaml]\t\t\t optional, setting client's jkubernetes.yaml")
    print ("\n\t[--exclude-jars exclude1.jar,exclude2.jar]\t optional, exclude jars, avoid jar conflict")
    print ("\n\t[-c key1=value1,key2=value2]\t\t\t optional, add key=value pair to configuration")
    print ("\nHelp:", "\n\thelp", "\n\thelp <command>")
    print ("\nDocumentation for the jkubernetes client can be found at https://github.com/gwisoft/jkubernetes/wiki/jkubernetes-Chinese-Documentation\n")


def get_jars_full(adir):
    ret = []
    temp = adir.strip()
    print (temp == "")
    
    if temp == "":
         return ret  
    files = os.listdir(adir)
    for f in files:
        if f.endswith(".jar") == False:
            continue
        filter = False
        for exclude_jar in EXCLUDE_JARS:
            if f.find(exclude_jar) >= 0:
                filter = True
                break
        
        if filter == True:
            print ("Don't add " + f + " to classpath")
        else:
            ret.append(adir + "/" + f)
    return ret


def unknown_command(*args):
    print ("Unknown command: [kubectl %s]" % ' '.join(sys.argv[1:]))
    print_usage()
    
def print_usage(command=None):
    """Print one help message or list of available commands"""
    if command != None:
        if command in COMMANDS:
            print (COMMANDS[command].__doc__ or 
                  "No documentation provided for <%s>" % command)
        else:
           print ("<%s> is not a valid command" % command)
    else:
        print_commands()

def parse_config_opts_and_args(args):
	curr = args[:]
	curr.reverse()
	config_list = []
	args_list = []
	
	while len(curr) > 0:
		token = curr.pop()
		if token == "-s":
			global API_SERVER_ADDRESS
			API_SERVER_ADDRESS = curr.pop()
		elif token == "-c":
			config_list.append(curr.pop())	
		elif token == "--config":
			global CUSTOM_CONF_FILE
			CUSTOM_CONF_FILE = curr.pop()			
		else:
			args_list.append(token)	
	print ("config_list=")
	print (config_list)
	print ("args_list=")
	print (args_list)
	return config_list, args_list
    
def parse_config_opts(config_list):
    global CONFIG_OPTS
    if len(config_list) > 0:
        for config in config_list:
            CONFIG_OPTS.append(config)         

def filter_array(array):
    ret = []
    for item in array:
        temp = item.strip()
        if temp != "":
            ret.append(temp)
    return ret           

def get_config_opts():
    global CONFIG_OPTS
    print ("-Dkubernetes.options=" + (','.join(CONFIG_OPTS)).replace(' ', "%%%%"))
    return "-Dkubernetes.options=" + (','.join(CONFIG_OPTS)).replace(' ', "%%%%")
    

#扩展的jar包入参    
def get_exclude_jars():
    global EXCLUDE_JARS
    return " -Dexclude.jars=" + (','.join(EXCLUDE_JARS))   

def create(args):
	"""
	sdsd
	"""
	pass

	args = parse_client_createopts(args)
	
	childopts = get_client_customopts() + get_exclude_jars() + get_client_createopts()
	print ("childopts=")
	print (childopts)
	exec_storm_class(
        "org.jkubernetes.kubectl.KubectlCreate",
        jvmtype="-client -Xms256m -Xmx256m",
        sysdirs=[JKUBERNETES_CONF_DIR, JKUBERNETES_DIR + "/bin",CUSTOM_CONF_FILE],
        args=args,
        childopts=childopts)

def kube(args):
	"""
	sdsd
	"""
	pass
	
	childopts = get_client_customopts() + get_exclude_jars()
	print ("childopts=")
	print (childopts)
	exec_storm_class(
        "org.jkubernetes.daemon.kube.KubeServer",
        jvmtype="-client -Xms256m -Xmx256m",
        sysdirs=[JKUBERNETES_CONF_DIR, JKUBERNETES_DIR + "/bin",CUSTOM_CONF_FILE],
        args=args,
        childopts=childopts)
	
def get_client_createopts():
	ret = (" -Dkubernetes.create.yaml=" + JKUBERNETES_CREATE_YAML_PATH + " -Dkubernetes.apiserver.address=" + API_SERVER_ADDRESS)
	return ret

def parse_client_createopts(args):
	print ("parse_client_createopts=")
	print (args)
	curr = args
	curr.reverse()
	args_list = []
	while len(curr) > 0:
		token = curr.pop()
		print (token == "-f")
		if token == "-f":
			global JKUBERNETES_CREATE_YAML_PATH
			JKUBERNETES_CREATE_YAML_PATH = curr.pop()
		else:
			args_list.append(token)
	print (args_list)		
	return 	args_list	
		
def exec_storm_class(klass, jvmtype="-server", sysdirs=[], args=[], childopts=""):
    
    args_str = " ".join(args)

    command = "java " + " -Dkubernetes.home=" + JKUBERNETES_DIR + " " + get_config_opts() + " " + childopts + " -cp " + get_classpath(sysdirs) + " " + klass + " " + args_str
    
    print ("Running: " + command)
    global STATUS
    STATUS = os.execvp("java", filter_array(command.split(" ")))

#系统自定义的配置入参    
def get_client_customopts():
    ret = (" -Dkubernetes.root.logger=INFO,stdout -Dlogback.configurationFile=" + JKUBERNETES_DIR +
           "/conf/client_logback.xml -Dlog4j.configuration=File:" + JKUBERNETES_DIR + 
           "/conf/client_log4j.properties")
    return ret

def get_classpath(extrajars):
    ret = []
    ret.extend(extrajars)
    print (1)
    ret.extend(get_jars_full(JKUBERNETES_DIR))
    print (2)
    ret.extend(get_jars_full(JKUBERNETES_DIR + "/lib"))
    ret.extend(get_jars_full(JKUBERNETES_DIR + "/lib/ext"))
    ret.extend(INCLUDE_JARS)
    return normclasspath(":".join(ret))
    
                     	        
def main():
	if len(sys.argv) <= 1:
		print_usage()
		sys.exit(-1)
			
	global CONFIG_OPTS
	config_list, args = parse_config_opts_and_args(sys.argv[1:])
	parse_config_opts(config_list)
	COMMAND = args[0]
	ARGS = args[1:]
	if COMMANDS.get(COMMAND) == None:
		unknown_command(COMMAND)
		sys.exit(-1)
	if len(ARGS) != 0 and ARGS[0] == "help":
		print_usage(COMMAND)
		sys.exit(0)
	try:
		(COMMANDS.get(COMMAND,"help"))(ARGS)
	except Exception as msg:
		print(msg)
		print_usage(COMMAND)
		sys.exit(-1)
	sys.exit(STATUS)

COMMANDS = {"create": create,"kube":kube}
		        
if __name__ == "__main__":
    #check_java()
    main()
