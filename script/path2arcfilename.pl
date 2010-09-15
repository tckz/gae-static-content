#!/usr/bin/perl

use utf8;
use strict;
use warnings;
no warnings 'uninitialized';

use Digest::SHA1 qw(sha1_hex);

my $head_chars = int($ARGV[0]);
$head_chars = 1 if $head_chars == 0;

my	$filename_prefix = $ARGV[1];
$filename_prefix = "filelist-" if $filename_prefix eq "";

my	%files;
while(<STDIN>)
{
	chomp;
	my	$path = $_;

	my	$digest = sha1_hex($path);
	my	$head = substr($digest, 0, $head_chars);

	my	$fh = $files{$head};
	if(!defined $fh)
	{
		open($fh, ">:utf8", "$filename_prefix$head") || die "$!";
		$files{$head} = $fh;
	}

	print $fh "$path\n";
}

