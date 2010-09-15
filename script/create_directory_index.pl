#!/usr/bin/perl

use utf8;
use strict;
use warnings;
no warnings 'uninitialized';

use Template;
use DateTime;
use DateTime::Format::W3CDTF;


my	$dir = $ARGV[0];
die "directory $dir not exist.\n" if(!-d $dir);

my	$html = $ARGV[1];
die "specify out-html\n" if($html eq "");

my	$title = $ARGV[2];
die "specify title\n" if($title eq "");

my	$regex = $ARGV[3];
die "specify regex\n" if($regex eq "");

sub commanize
{
	my($d) = @_;
	$d =~ s/^(.+)(...)$/my $a = $2;commanize($1).",$a";/e;
	$d;
}

opendir(DIR, $dir);
my	@entries = map { 
	my	$f = DateTime::Format::W3CDTF->new;
	my	$path = "$dir/$_";
	my	@st = stat($path);
	my	$dt = DateTime->from_epoch(epoch => $st[9]);
	{
		name => $_ . (-d $path ? "/" : ""),
		mtime => $f->format_datetime($dt),
		size => commanize($st[7]),
	} 
} sort grep { $_ =~ /$regex/o ;} readdir DIR;
closedir DIR;

my	$vars = {
	title => $title,
	entries => \@entries,
};

my	$template = <<'EOF';
<html>
<head>
<meta http-equiv="Content-type" content="text/html; charset=utf-8">
<style type="text/css">
<!--
TABLE {
	font-family: monospace;
}
TH {
	border-bottom: thin dashed black;
}
TD.name {
	padding-left: 1em;
}
TD.size {
	padding-left: 1em;
	text-align: right;
}
TD.mtime {
	padding-left: 1em;
}
-->
</style>
<title>[% title | html%]</title>
</head>
<body>
	<table>
		<tr>
			<th class="name">name</th>
			<th class="size">size</th>
			<th class="mtime">mtime</th>
		</tr>
		[% FOREACH rec IN entries %]
		<tr>
			<td class="name"><a href="[% rec.name | html %]">[% rec.name | html %]</a></td>
			<td class="size">[% rec.size | html %]</td>
			<td class="mtime">[% rec.mtime | html %]</td>
		<tr>
		[% END %]
	</table>
</body>
</html>
EOF

my	$tt = Template->new();
$tt->process(\$template, $vars, $html) || die "$!";

