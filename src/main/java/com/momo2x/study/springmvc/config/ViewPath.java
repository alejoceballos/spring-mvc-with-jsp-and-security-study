package com.momo2x.study.springmvc.config;

public final class ViewPath {

    public static final class URL {
        public final static class ROOT {
            public static final String PATH = "/";
            public static final String OTHER = ROOT.PATH + "other";
        }

        public final static class USER {
            public static final String PATH = ROOT.PATH + "user/";
            public static final String MAIN = PATH + "main";
            public static final String OTHER = PATH + "other";
        }

        public final static class ADMIN {
            public static final String PATH = ROOT.PATH + "admin/";
            public static final String ADMIN = PATH + "admin";
            public static final String OTHER = PATH + "other";
        }
    }

}
