#!/bin/sh
cp ../_OUTPUT___WorkingEclipseSite_features_and_plugins__/_OUTPUT/_DEPENDENCY_SET/dependencies.fm fodder/dependencies.fm
cp ../_OUTPUT___WorkingEclipseSite_features_and_plugins__/_OUTPUT/_DEPENDENCY_SET/constraints.fm fodder/constraints.fm

sbt run > fodder/output/results.ttlg
echo Done.