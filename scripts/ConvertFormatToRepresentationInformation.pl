#!/bin/perl
use strict;
use warnings;
use JSON;
use Data::Dumper;

for my $file (`ls format`) {
	chomp $file;
	open(my $fh, '<', "format/$file") or die "Não foi possível abrir o ficheiro JSON do format $!";
	open(my $fh2, '>', "representation-information/$file") or die "Não foi possível abrir o ficheiro JSON do representation information $!";

	my $format = decode_json <$fh>;
	my %representation_information = ();

	## representation information default values
	$representation_information{'family'} = "fileformat";
	$representation_information{'filters'} = [];
	$representation_information{'relations'} = [];
	$representation_information{'updatedBy'} = "admin";
	$representation_information{'updatedOn'} = 1515421588276;
	$representation_information{'support'} = "SUPPORTED";

	## representation information simple values
	$representation_information{'id'} = $format->{'id'};
	$representation_information{'name'} = $format->{'name'};
	$representation_information{'description'} = $format->{'definition'};
	$representation_information{'categories'} = \@{$format->{'categories'}};

	## representation information extra values

	# getting extensions
	my $extensions = "";
	for my $extension (@{$format->{'extensions'}}) {
		$extensions .= $extension.", ";
	}
	$extensions = substr $extensions, 0, -2; 

	# getting mimetypes
	my $mimetypes = "";
	for my $mimetype (@{$format->{'mimetypes'}}) {
		$mimetypes .= $mimetype.", ";
	}
	$mimetypes = substr $mimetypes, 0, -2; 

	# getting pronoms
	my $pronoms = "";
	for my $pronom (@{$format->{'pronoms'}}) {
		$pronoms .= $pronom.", ";
	}
	$pronoms = substr $pronoms, 0, -2; 

	# getting open format
	my $disclosure = "";
	my $openFormat = "".$format->{'openFormat'};
	if($openFormat eq "true") {
		$disclosure = "Is open format.";
	}

	$representation_information{'extras'} = "<?xml version=\"1.0\" encoding=\"utf-8\"?><metadata><field name=\"disclosure\">$disclosure</field><field name=\"filenameExtension\">$extensions</field><field name=\"internetMediaType\">$mimetypes</field><field name=\"pronomPUID\">$pronoms</field></metadata>";

	## representation information web relations
	$representation_information{'relations'}[0] = ();
	$representation_information{'relations'}[0]->{'link'} = "http://".$format->{'provenanceInformation'};
	$representation_information{'relations'}[0]->{'objectType'} = "WEB";
	$representation_information{'relations'}[0]->{'relationType'} = "SPECIFIED_BY";
	$representation_information{'relations'}[0]->{'title'} = "Provenance information";
	
	my $i = 1;
	for my $website (@{$format->{'websites'}}) {
		$representation_information{'relations'}[$i] = ();
		$representation_information{'relations'}[$i]->{'link'} = $website;
		$representation_information{'relations'}[$i]->{'objectType'} = "WEB";
		$representation_information{'relations'}[$i]->{'relationType'} = "SPECIFIED_BY";
		$representation_information{'relations'}[$i]->{'title'} = $website;
		$i++;
	}


	my $ri_json = encode_json \%representation_information;
	print $fh2 $ri_json;

	close($fh);
	close($fh2);
}