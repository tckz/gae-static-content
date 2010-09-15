#!/bin/sh

# run on CentOS 5.5

mypath=`cd \`dirname $0\` && /bin/pwd`

usage()
{
	echo "usage: $0 [-z prefix_zip] [-e exclude_txt] /path/to/docs /path/to/out /path/to/temp" 1>&2
	exit 1
}

opt_prefix_zip=static-document-
opt_exclude_txt=/dev/null

while getopts e:z: c
do
	case $c in
		z)
			opt_prefix_zip=$OPTARG
			;;
		e)
			opt_exclude_txt=$OPTARG
			;;
		?)
			usage
			exit 1
			;;
	esac
done

shift  $(($OPTIND - 1))

path_documents=$1
path_out=$2
path_tmp=$3

if [ -z "$path_documents" ] || [ -z "$path_tmp" ] || [ -z "$path_out" ]
then
	usage
	exit 1
fi

if [ ! -d "$path_documents" ]
then
	echo "*** document directory $path_documents not exist." 1>&2
	exit 1
fi

if [ ! -e "$opt_exclude_txt" ]
then
	echo "*** exlucde_txt $opt_exclude_txt not exist." 1>&2
	exit 1
fi

prefix_filelist=filelist
dt=`date +%Y%m%d%H`
arc="$path_tmp/documents-$dt.tgz"


for i in "$path_out" "$path_tmp"
do
	if [ ! -d $i ]
	then
		mkdir -p $i
	fi
done

echo "Creating $arc" 1>&2
(cd "$path_documents" && tar cfz "$arc" --exclude-from="$opt_exclude_txt" *)

/bin/rm -f "$path_out/$opt_prefix_zip-"*
/bin/rm -f "$path_tmp/$prefix_filelist-"*
tar tfz "$arc" | (cd "$path_tmp" && "$mypath"/path2arcfilename.pl 1 "$prefix_filelist-")

for i in "$path_tmp/$prefix_filelist-"*
do
	head=`echo $i | sed -e 's/^.*-//'`
	zipfile="$path_out/$opt_prefix_zip-$head.zip"
	echo Creating $zipfile 1>&2
	(cd "$path_documents" && zip -q "$zipfile" -@ < "$i")
done

